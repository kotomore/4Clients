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
import ru.set404.clients.dto.telegram.AppointmentMSG;
import ru.set404.clients.dto.telegram.AvailabilityMSG;
import ru.set404.clients.dto.telegram.ScheduleMSG;
import ru.set404.clients.dto.telegram.TelegramMessage;
import ru.set404.clients.exceptions.AgentNotFoundException;
import ru.set404.clients.exceptions.AppointmentNotFoundException;
import ru.set404.clients.exceptions.ServiceNotFoundException;
import ru.set404.clients.models.Agent;
import ru.set404.clients.models.AgentService;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.services.ManagementService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
                Agent agent;
                try {
                    agent = managementService.findAgentById(message.getAgentId());
                } catch (AgentNotFoundException ex) {
                    agent = new Agent();
                    agent.setPhone(message.getAgentId());
                }
                template.convertAndSend(telegramExchange.getName(), "telegram_key.agent", agent);
            }

            case SERVICE_INFO -> {
                AgentService service;
                try {
                    service = managementService.findService(message.getAgentId());
                } catch (ServiceNotFoundException ex) {
                    service = new AgentService();
                    service.setAgentId(message.getAgentId());
                }
                template.convertAndSend(telegramExchange.getName(), "telegram_key.service", service);
            }

            case SCHEDULES -> {
                AvailabilityMSG availabilityMSG;
                try {
                    availabilityMSG = managementService.findAvailableTimeForTelegram(message.getAgentId());
                } catch (ServiceNotFoundException ex) {
                    availabilityMSG = new AvailabilityMSG();
                    availabilityMSG.setAgentId(message.getAgentId());
                }
                template.convertAndSend(telegramExchange.getName(), "telegram_key.schedule", availabilityMSG);
            }

            case APPOINTMENTS -> {
                List<Appointment> appointments;
                try {
                    appointments = managementService.findAllAppointments(message.getAgentId());
                    for (Appointment appointment : appointments) {
                        if (appointment.getDate().isAfter(LocalDate.now().minusDays(1))) {
                            AppointmentMSG appointmentMSG = new AppointmentMSG();
                            appointmentMSG.setStartTime(appointment.getTimeSlot().getStartTime().toString());
                            appointmentMSG.setEndTime(appointment.getTimeSlot().getEndTime().toString());
                            appointmentMSG.setDate(appointment.getDate().toString());
                            appointmentMSG.setAgentId(message.getAgentId());
                            appointmentMSG.setClientName(appointment.getClient().getName());
                            appointmentMSG.setClientPhone(appointment.getClient().getPhone());
                            appointmentMSG.setType(AppointmentMSG.Type.OLD);

                            AgentService agentService = managementService.findService(message.getAgentId());
                            appointmentMSG.setServiceName(agentService.getId());

                            template.convertAndSend(telegramExchange.getName(), "telegram_key.appointment", appointmentMSG);
                        }
                    }
                } catch (AppointmentNotFoundException ignore) {
                }
            }

            case REGISTER_BOT -> {
                Agent agent = managementService.findOrCreateAgentByPhone(message.getAgentId());
                template.convertAndSend(telegramExchange.getName(), "telegram_key.register", agent);
            }
        }
    }

    @RabbitListener(queues = "telegram_update_service", returnExceptions = "false")
    public void receiveServiceUpdate(AgentService service) {
        managementService.addOrUpdateService(service.getAgentId(), service);
        template.convertAndSend(telegramExchange.getName(), "telegram_key.service", service);
    }

    @RabbitListener(queues = "telegram_update_agent", returnExceptions = "false")
    public void receiveAgentUpdate(Agent agent) {
        AgentDTO agentDTO = modelMapper.map(agent, AgentDTO.class);
        managementService.updateAgent(agent.getId(), agentDTO);
        template.convertAndSend(telegramExchange.getName(), "telegram_key.agent", agent);
    }

    @RabbitListener(queues = "telegram_update_schedule", returnExceptions = "false")
    public void receiveScheduleUpdate(ScheduleMSG scheduleMSG) {
        AgentService agentService = managementService.findService(scheduleMSG.getAgentId());

        TimeSlotDTO timeSlotDTO = new TimeSlotDTO();
        timeSlotDTO.setServiceId(scheduleMSG.getServiceId());
        timeSlotDTO.setDateStart(LocalDate.parse(scheduleMSG.getDateStart()));
        timeSlotDTO.setDateEnd(LocalDate.parse(scheduleMSG.getDateEnd()));
        timeSlotDTO.setTimeStart(LocalTime.parse(scheduleMSG.getTimeStart()));
        timeSlotDTO.setTimeEnd(LocalTime.parse(scheduleMSG.getTimeEnd()));
        timeSlotDTO.setServiceId(agentService.getId());

        managementService.addAvailableTime(scheduleMSG.getAgentId(), timeSlotDTO);

        AvailabilityMSG availabilityMSG = managementService.findAvailableTimeForTelegram(scheduleMSG.getAgentId());

        template.convertAndSend(telegramExchange.getName(), "telegram_key.schedule", availabilityMSG);

    }
}