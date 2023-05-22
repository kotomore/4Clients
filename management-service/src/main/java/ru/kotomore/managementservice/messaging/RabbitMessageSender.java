package ru.kotomore.managementservice.messaging;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Component;
import ru.kotomore.managementservice.dto.AgentDTO;
import ru.kotomore.managementservice.dto.TimeSlotDTO;
import ru.kotomore.managementservice.exceptions.AgentNotFoundException;
import ru.kotomore.managementservice.exceptions.AppointmentNotFoundException;
import ru.kotomore.managementservice.exceptions.ServiceNotFoundException;
import ru.kotomore.managementservice.models.AgentService;
import ru.kotomore.managementservice.models.Appointment;
import ru.kotomore.managementservice.services.ManagementService;
import telegram.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Component
public class RabbitMessageSender {

    private ManagementService managementService;
    private AmqpTemplate template;
    private TopicExchange telegramExchange;
    private ModelMapper modelMapper;

    public void sendAgentInfo(TelegramMessage message) {
        AgentMSG agent;
        try {
            agent = modelMapper.map(managementService.findAgentById(message.getAgentId()), AgentMSG.class);
        } catch (AgentNotFoundException ex) {
            agent = new AgentMSG();
            agent.setPhone(message.getAgentId());
        }
        template.convertAndSend(telegramExchange.getName(), "telegram_key.agent", agent);
    }

    public void sendServiceInfo(TelegramMessage message) {
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

    public void sendAvailability(TelegramMessage message) {
        AvailabilityMSG availabilityMSG;
        try {
            availabilityMSG = getTelegramAvailabilityMSG(message.getAgentId());
        } catch (AppointmentNotFoundException | ServiceNotFoundException ex) {
            availabilityMSG = new AvailabilityMSG();
            availabilityMSG.setAgentId(message.getAgentId());
        }
        template.convertAndSend(telegramExchange.getName(), "telegram_key.schedule", availabilityMSG);
    }

    public void sendScheduleDelete(TelegramMessage message) {
        managementService.deleteAllAvailableTime(message.getAgentId());
        AvailabilityMSG availabilityMSG = new AvailabilityMSG();
        availabilityMSG.setAgentId(message.getAgentId());
        template.convertAndSend(telegramExchange.getName(), "telegram_key.schedule", availabilityMSG);
    }

    public void sendAppointmentsDelete(TelegramMessage message) {
        managementService.deleteAllAppointments(message.getAgentId());
        sendErrorMessage(message.getAgentId(), "Записей не обнаружено");
    }

    public void sendAppointments(TelegramMessage message) {
        List<Appointment> appointments;
        try {
            appointments = managementService.findAllAppointments(message.getAgentId());
            AgentService agentService = managementService.findService(message.getAgentId());

            List<AppointmentMSG> appointmentMSGS = new ArrayList<>();
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
                    appointmentMSG.setServiceName(agentService.getId());

                    appointmentMSGS.add(appointmentMSG);
                }
            }

            template.convertAndSend(telegramExchange.getName(), "telegram_key.all_appointment", appointmentMSGS);

        } catch (AppointmentNotFoundException | ServiceNotFoundException exception) {
            sendErrorMessage(message.getAgentId(), exception.getMessage());
        }
    }

    public void sendRegisterMessage(TelegramMessage message) {
        AgentMSG agent = modelMapper.map(managementService.findOrCreateAgentByPhone(message.getAgentId()),
                AgentMSG.class);
        template.convertAndSend(telegramExchange.getName(), "telegram_key.register", agent);
    }

    private AvailabilityMSG getTelegramAvailabilityMSG(String agentId) {
        AvailabilityMSG availabilityMSG = new AvailabilityMSG();
        List<Appointment> appointments = new ArrayList<>();

        try {
            appointments = managementService.findAllAppointments(agentId);
        } catch (AppointmentNotFoundException ignore) {
        }

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

        appointments.forEach(appointment -> {
            Availability telegramAvailability = new Availability();
            telegramAvailability.setDate(appointment.getStartTime().toLocalDate());
            telegramAvailability.setStartTime(appointment.getStartTime().toLocalTime());
            telegramAvailability.setEndTime(appointment.getEndTime().toLocalTime());
            telegramAvailability.setBooked(true);
            availabilities.add(telegramAvailability);
        });

        availabilities.sort(Comparator.comparing(Availability::getDate).thenComparing(Availability::getStartTime));
        availabilityMSG.setAgentId(agentId);
        availabilityMSG.setAvailabilities(availabilities);
        return availabilityMSG;
    }

    public void sendUpdatedService(AgentServiceMSG serviceMessage) {
        AgentService service = modelMapper.map(serviceMessage, AgentService.class);
        service = managementService.addOrUpdateService(service.getAgentId(), service);
        serviceMessage = modelMapper.map(service, AgentServiceMSG.class);
        template.convertAndSend(telegramExchange.getName(), "telegram_key.service", serviceMessage);
    }

    public void sendUpdatedAgent(AgentMSG agent) {
        AgentDTO agentDTO = modelMapper.map(agent, AgentDTO.class);
        agent = modelMapper.map(managementService.updateAgent(agent.getId(), agentDTO), AgentMSG.class);
        template.convertAndSend(telegramExchange.getName(), "telegram_key.agent", agent);
    }

    public void sendUpdatedSchedule(ScheduleMSG scheduleMSG) {
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
        } catch (AppointmentNotFoundException | ServiceNotFoundException exception) {
            ErrorMSG errorMSG = new ErrorMSG();
            errorMSG.setAgentId(scheduleMSG.getAgentId());
            errorMSG.setMessage(exception.getMessage());
            template.convertAndSend(telegramExchange.getName(), "telegram_key.error", errorMSG);
        }
    }

    private boolean isValidAgentService(AgentService agentService) {
        if (agentService.getDuration() < 15 || agentService.getDescription() == null ||
                agentService.getPrice() == 0 || agentService.getName() == null) {
            sendErrorMessage(agentService.getAgentId(), "Заполнены не все поля услуги");
            return false;
        }
        return true;
    }

    public void sendErrorMessage(String agentId, String text) {
        ErrorMSG errorMSG = new ErrorMSG();
        errorMSG.setAgentId(agentId);
        errorMSG.setMessage(text);
        template.convertAndSend(telegramExchange.getName(), "telegram_key.error", errorMSG);
    }
}