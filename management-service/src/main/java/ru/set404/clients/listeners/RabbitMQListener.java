package ru.set404.clients.listeners;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.dto.TimeSlotDTO;
import ru.set404.clients.exceptions.AgentNotFoundException;
import ru.set404.clients.exceptions.AppointmentNotFoundException;
import ru.set404.clients.exceptions.ServiceNotFoundException;
import ru.set404.clients.models.AgentService;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.services.ManagementService;
import telegram.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@EnableRabbit
@Slf4j
@AllArgsConstructor
@Component
public class RabbitMQListener {

    private ManagementService managementService;
    private AmqpTemplate template;
    private TopicExchange telegramExchange;
    private ModelMapper modelMapper;

    @RabbitListener(queues = "telegram_queue_from", returnExceptions = "false")
    public void receive(TelegramMessage message) {
        log.info("Message from telegram - " + message.getAction());
        switch (message.getAction()) {
            case AGENT_INFO -> {
                AgentMSG agent;
                try {
                    agent = modelMapper.map(managementService.findAgentById(message.getAgentId()), AgentMSG.class);
                } catch (AgentNotFoundException ex) {
                    agent = new AgentMSG();
                    agent.setPhone(message.getAgentId());
                }
                template.convertAndSend(telegramExchange.getName(), "telegram_key.agent", agent);
            }

            case SERVICE_INFO -> {
                AgentServiceMSG serviceMSG;
                try {
                    serviceMSG = modelMapper.map(managementService.findService(message.getAgentId()),
                            AgentServiceMSG.class);
                } catch (ServiceNotFoundException ex) {
                    serviceMSG = new AgentServiceMSG();
                    serviceMSG.setAgentId(message.getAgentId());
                }
                template.convertAndSend(telegramExchange.getName(), "telegram_key.service", serviceMSG);
            }

            case SCHEDULES -> {
                AvailabilityMSG availabilityMSG;
                try {
                    availabilityMSG = getTelegramAvailabilityMSG(message.getAgentId());
                } catch (ServiceNotFoundException ex) {
                    availabilityMSG = new AvailabilityMSG();
                    availabilityMSG.setAgentId(message.getAgentId());
                }
                template.convertAndSend(telegramExchange.getName(), "telegram_key.schedule", availabilityMSG);
            }

            case SCHEDULE_DELETE -> {
                managementService.deleteAllAvailableTime(message.getAgentId());
                AvailabilityMSG availabilityMSG = new AvailabilityMSG();
                availabilityMSG.setAgentId(message.getAgentId());
                template.convertAndSend(telegramExchange.getName(), "telegram_key.schedule", availabilityMSG);
            }

            case APPOINTMENTS -> {
                List<Appointment> appointments;
                try {
                    appointments = managementService.findAllAppointments(message.getAgentId());
                    for (Appointment appointment : appointments) {
                        if (appointment.getStartTime().isAfter(LocalDateTime.now().minusDays(1))) {
                            AppointmentMSG appointmentMSG = new AppointmentMSG();
                            appointmentMSG.setDate(appointment.getStartTime().toLocalDate());
                            appointmentMSG.setStartTime(appointment.getStartTime().toLocalTime());
                            appointmentMSG.setEndTime(appointment.getEndTime().toLocalTime());
                            appointmentMSG.setAgentId(message.getAgentId());
                            appointmentMSG.setClientName(appointment.getClient().getName());
                            appointmentMSG.setClientPhone(appointment.getClient().getPhone());
                            appointmentMSG.setType(AppointmentMSG.Type.OLD);

                            AgentService agentService = managementService.findService(message.getAgentId());
                            appointmentMSG.setServiceName(agentService.getId());

                            template.convertAndSend(telegramExchange.getName(), "telegram_key.appointment", appointmentMSG);
                        }
                    }
                } catch (AppointmentNotFoundException | ServiceNotFoundException exception) {
                    ErrorMSG errorMSG = new ErrorMSG();
                    errorMSG.setAgentId(message.getAgentId());
                    errorMSG.setMessage(exception.getMessage());
                    template.convertAndSend(telegramExchange.getName(), "telegram_key.error", errorMSG);
                }
            }

            case REGISTER_BOT -> {
                AgentMSG agent = modelMapper.map(managementService.findOrCreateAgentByPhone(message.getAgentId()),
                        AgentMSG.class);
                template.convertAndSend(telegramExchange.getName(), "telegram_key.register", agent);
            }
        }
    }

    @RabbitListener(queues = "telegram_update_service", returnExceptions = "false")
    public void receiveServiceUpdate(AgentServiceMSG serviceMessage) {
        if (isValidServiceMessage(serviceMessage)) {
            AgentService service = modelMapper.map(serviceMessage, AgentService.class);
            service = managementService.addOrUpdateService(service.getAgentId(), service);
            serviceMessage = modelMapper.map(service, AgentServiceMSG.class);
            template.convertAndSend(telegramExchange.getName(), "telegram_key.service", serviceMessage);
        }
    }

    @RabbitListener(queues = "telegram_update_agent", returnExceptions = "false")
    public void receiveAgentUpdate(AgentMSG agent) {
        AgentDTO agentDTO = modelMapper.map(agent, AgentDTO.class);
        agent = modelMapper.map(managementService.updateAgent(agent.getId(), agentDTO), AgentMSG.class);
        template.convertAndSend(telegramExchange.getName(), "telegram_key.agent", agent);
    }

    @RabbitListener(queues = "telegram_update_schedule", returnExceptions = "false")
    public void receiveScheduleUpdate(ScheduleMSG scheduleMSG) {
        if (isValidScheduleMessage(scheduleMSG)) {
            try {
                AgentService agentService = managementService.findService(scheduleMSG.getAgentId());

                if (isValidAgentService(agentService)) {
                    TimeSlotDTO timeSlotDTO = new TimeSlotDTO();
                    timeSlotDTO.setServiceId(scheduleMSG.getServiceId());
                    timeSlotDTO.setDateStart(scheduleMSG.getDateStart());
                    timeSlotDTO.setDateEnd(scheduleMSG.getDateEnd());
                    timeSlotDTO.setTimeStart(scheduleMSG.getTimeStart());
                    timeSlotDTO.setTimeEnd(scheduleMSG.getTimeEnd());
                    timeSlotDTO.setServiceId(agentService.getId());

                    managementService.addAvailableTime(scheduleMSG.getAgentId(), timeSlotDTO);

                    AvailabilityMSG availabilityMSG = getTelegramAvailabilityMSG(scheduleMSG.getAgentId());

                    template.convertAndSend(telegramExchange.getName(), "telegram_key.schedule", availabilityMSG);
                }
            } catch (ServiceNotFoundException exception) {
                ErrorMSG errorMSG = new ErrorMSG();
                errorMSG.setAgentId(scheduleMSG.getAgentId());
                errorMSG.setMessage(exception.getMessage());
                template.convertAndSend(telegramExchange.getName(), "telegram_key.error", errorMSG);
            }
        }
    }

    private AvailabilityMSG getTelegramAvailabilityMSG(String agentId) {
        AvailabilityMSG availabilityMSG = new AvailabilityMSG();

        List<Availability> availabilities = managementService.findAvailableTimeForTelegram(agentId)
                .stream()
                .map(availability -> {
                    Availability telegramAvailability = new Availability();
                    telegramAvailability.setDate(availability.getStartTime().toLocalDate());
                    telegramAvailability.setStartTime(availability.getStartTime().toLocalTime());
                    telegramAvailability.setEndTime(availability.getEndTime().toLocalTime());
                    return telegramAvailability;
                })
                .collect(Collectors.toList());
        availabilityMSG.setAgentId(agentId);
        availabilityMSG.setAvailabilities(availabilities);
        return availabilityMSG;
    }

    private boolean isValidServiceMessage(AgentServiceMSG agentServiceMSG) {
        final int MIN_SERVICE_DURATION = 15;
        if (agentServiceMSG.getDuration() != 0 && agentServiceMSG.getDuration() < MIN_SERVICE_DURATION) {
            sendErrorMessage(agentServiceMSG.getAgentId(), "Длительность услуги не может быть менее 15 минут");
            return false;
        }
        return true;
    }

    private boolean isValidScheduleMessage(ScheduleMSG scheduleMSG) {
        if (scheduleMSG.getTimeStart().isAfter(scheduleMSG.getTimeEnd()) ||
                scheduleMSG.getDateStart().isAfter(scheduleMSG.getDateEnd()) ||
                scheduleMSG.getDateStart().isBefore(LocalDate.now())) {
            sendErrorMessage(scheduleMSG.getAgentId(), "Время или дата указаны неверно");
            return false;
        }
        return true;
    }

    private boolean isValidAgentService(AgentService agentService) {
        if (agentService.getDuration() < 15 || agentService.getDescription() == null ||
                agentService.getPrice() == 0 || agentService.getName() == null) {
            sendErrorMessage(agentService.getAgentId(), "Заполнены не все поля услуги");
            return false;
        }
        return true;
    }

    private void sendErrorMessage(String agentId, String text) {
        ErrorMSG errorMSG = new ErrorMSG();
        errorMSG.setAgentId(agentId);
        errorMSG.setMessage(text);
        template.convertAndSend(telegramExchange.getName(), "telegram_key.error", errorMSG);
    }
}