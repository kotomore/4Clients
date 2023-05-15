package ru.kotomore.clientservice.messaging;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Component;
import ru.kotomore.clientservice.exceptions.TelegramServiceNotAvailableException;
import ru.kotomore.clientservice.models.Appointment;
import telegram.AppointmentMSG;

@Component
@Slf4j
@AllArgsConstructor
public class RabbitMessageSender {
    private final AmqpTemplate template;
    private TopicExchange telegramExchange;

    public void sendTelegramNotification(Appointment appointment) throws TelegramServiceNotAvailableException{
        AppointmentMSG appointmentMSG = new AppointmentMSG();
        appointmentMSG.setAppointmentId(appointment.getId());
        appointmentMSG.setAgentId(appointment.getAgentId());
        appointmentMSG.setDate(appointment.getStartTime().toLocalDate());
        appointmentMSG.setStartTime(appointment.getStartTime().toLocalTime());
        appointmentMSG.setEndTime(appointment.getEndTime().toLocalTime());
        appointmentMSG.setClientName(appointment.getClient().getName());
        appointmentMSG.setClientPhone(appointment.getClient().getPhone());
        appointmentMSG.setType(AppointmentMSG.Type.NEW);

        Boolean isSend = (Boolean) template.convertSendAndReceive(telegramExchange.getName(), "telegram_key.appointment", appointmentMSG);
        if (isSend == null || isSend.equals(Boolean.FALSE)) {
            log.debug("Client service send message | Telegram service is not available");
            throw new TelegramServiceNotAvailableException();
        }
    }
}
