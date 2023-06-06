package ru.kotomore.managementservice.messaging;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.kotomore.managementservice.exceptions.AppointmentNotFoundException;
import ru.kotomore.managementservice.services.ManagementService;
import telegram.*;

import java.time.LocalDate;

@EnableRabbit
@Slf4j
@AllArgsConstructor
@Component
public class RabbitMessageListener {

    private ManagementService managementService;
    private AmqpTemplate template;
    private TopicExchange telegramExchange;
    private RabbitMessageSender rabbitSender;

    @RabbitListener(queues = "telegram_queue_from", returnExceptions = "false")
    public void receive(TelegramMessage message) {
        log.info("Message from telegram - " + message.getAction());
        switch (message.getAction()) {
            case AGENT_INFO -> rabbitSender.sendAgentInfo(message);
            case SERVICE_INFO -> rabbitSender.sendServiceInfo(message);
            case SCHEDULES -> rabbitSender.sendAvailability(message);
            case SCHEDULE_DELETE -> rabbitSender.sendScheduleDelete(message);
            case APPOINTMENTS -> rabbitSender.sendAppointments(message);
            case APPOINTMENTS_DELETE -> rabbitSender.sendAppointmentsDelete(message);
            case REGISTER_BOT -> rabbitSender.sendRegisterMessage(message);
            case SETTINGS -> rabbitSender.sendSettingsMessage(message);
        }
    }

    @RabbitListener(queues = "telegram_update_service", returnExceptions = "false")
    public void receiveServiceUpdate(AgentServiceMSG serviceMessage) {
        if (isValidServiceMessage(serviceMessage)) {
            rabbitSender.sendUpdatedService(serviceMessage);
        }
    }

    @RabbitListener(queues = "telegram_update_agent", returnExceptions = "false")
    public void receiveAgentUpdate(AgentMSG agent) {
        rabbitSender.sendUpdatedAgent(agent);
    }

    @RabbitListener(queues = "telegram_update_schedule", returnExceptions = "false")
    public void receiveScheduleUpdate(ScheduleMSG scheduleMSG) {
        if (isValidScheduleMessage(scheduleMSG)) {
            rabbitSender.sendUpdatedSchedule(scheduleMSG);
        }
    }

    @RabbitListener(queues = "telegram_update_settings", returnExceptions = "false")
    public void receiveSettingsUpdate(SettingsMSG settingsMSG) {
        rabbitSender.sendUpdatedSettings(settingsMSG);
    }

    @RabbitListener(queues = "telegram_delete_appointment", returnExceptions = "false")
    public void receiveAppointmentDelete(AppointmentMSG appointmentMSG) {
        try {
            managementService.deleteAppointment(appointmentMSG.getAgentId(), appointmentMSG.getAppointmentId());
        } catch (AppointmentNotFoundException exception) {
            rabbitSender.sendErrorMessage(appointmentMSG.getAgentId(), exception.getMessage());
        }
    }

    @RabbitListener(queues = "telegram_add_break", returnExceptions = "false")
    public void receiveAddBreak(ScheduleMSG scheduleMSG) {
        rabbitSender.sendUpdatedBreak(scheduleMSG);
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

    private void sendErrorMessage(String agentId, String text) {
        ErrorMSG errorMSG = new ErrorMSG();
        errorMSG.setAgentId(agentId);
        errorMSG.setMessage(text);
        template.convertAndSend(telegramExchange.getName(), "telegram_key.error", errorMSG);
    }
}