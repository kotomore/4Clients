package ru.set404.telegramservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.set404.telegramservice.dto.telegram.*;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.telegram.WriteReadBot;
import ru.set404.telegramservice.telegram.keyboards.InlineKeyboardMaker;
import ru.set404.telegramservice.telegram.keyboards.ReplyKeyboardMaker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TelegramMessageServiceTest {

    @Mock
    private ReplyKeyboardMaker replyKeyboardMaker;

    @Mock
    private InlineKeyboardMaker inlineKeyboardMaker;

    @Mock
    private WriteReadBot writeReadBot;

    @InjectMocks
    private TelegramMessageService telegramMessageService;

    AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendSuccessRegMessage() throws TelegramApiException {
        TelegramUser user = new TelegramUser();
        user.setChatId("123");

        SendMessage sendMessage = new SendMessage(user.getChatId(), "Регистрация завершена\n*Выберите пункт меню*");
        sendMessage.enableMarkdown(true);

        when(replyKeyboardMaker.getMainMenuKeyboard()).thenReturn(null);

        telegramMessageService.sendSuccessRegMessage(user);

        verify(writeReadBot, times(1)).execute(sendMessage);
    }

    @Test
    public void testSendAgentServiceMessage() throws TelegramApiException {
        TelegramUser user = new TelegramUser();
        user.setChatId("123");

        AgentServiceMSG service = new AgentServiceMSG();
        service.setName("Test Service");
        service.setDescription("This is a test service");
        service.setDuration(30);
        service.setPrice(10.99);

        Mockito.when(inlineKeyboardMaker.getServiceInlineButton()).thenReturn(new InlineKeyboardMarkup());

        telegramMessageService.sendAgentServiceMessage(user, service);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(writeReadBot).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertTrue(sendMessage.getText().contains("Test Service"));
        assertTrue(sendMessage.getText().contains("This is a test service"));
        assertTrue(sendMessage.getText().contains("30"));
        assertTrue(sendMessage.getText().contains("10.99"));
        assertNotNull(sendMessage.getReplyMarkup());
        assertTrue(sendMessage.getReplyMarkup() instanceof InlineKeyboardMarkup);
        assertEquals(user.getChatId(), sendMessage.getChatId());
    }

    @Test
    public void testSendAgentInfoMessage() throws TelegramApiException {
        TelegramUser user = new TelegramUser();
        user.setChatId("123");

        AgentMSG agentMSG = new AgentMSG();
        agentMSG.setName("John Doe");

        Mockito.when(inlineKeyboardMaker.getAgentInlineButton()).thenReturn(new InlineKeyboardMarkup());

        telegramMessageService.sendAgentInfoMessage(user, agentMSG);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(writeReadBot).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertTrue(sendMessage.getText().contains("John Doe"));
        assertNotNull(sendMessage.getReplyMarkup());
        assertTrue(sendMessage.getReplyMarkup() instanceof InlineKeyboardMarkup);
        assertEquals(user.getChatId(), sendMessage.getChatId());
    }

    @Test
    public void testSendAgentAppointmentsMessage() throws TelegramApiException {
        TelegramUser user = new TelegramUser();
        user.setChatId("123");

        AppointmentMSG appointmentMSG = new AppointmentMSG();
        appointmentMSG.setType(AppointmentMSG.Type.NEW);
        appointmentMSG.setDate(LocalDate.now().toString());
        appointmentMSG.setStartTime(LocalTime.of(10, 0).toString());
        appointmentMSG.setEndTime(LocalTime.of(11, 0).toString());
        appointmentMSG.setClientName("John Doe");
        appointmentMSG.setClientPhone("1234567890");

        telegramMessageService.sendAgentAppointmentsMessage(user, appointmentMSG);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(writeReadBot).execute(sendMessageCaptor.capture());

        SendMessage sendMessage = sendMessageCaptor.getValue();

        assertThat(sendMessage.getText()).contains("*Новая заявка:*");
        assertThat(sendMessage.getText()).contains("*Дата*: " + appointmentMSG.getDate());
        assertThat(sendMessage.getText()).contains("*Время*:\n    Начало: " + appointmentMSG.getStartTime());
        assertThat(sendMessage.getText()).contains("*Клиент:*\n    Имя: " + appointmentMSG.getClientName());
        assertThat(sendMessage.getText()).contains("Телефон: `" + appointmentMSG.getClientPhone() + "`");
    }

    @Test
    public void testSendAgentSchedule() throws TelegramApiException {
        TelegramUser user = new TelegramUser();
        user.setChatId("123");

        AvailabilityMSG availabilityMSG = new AvailabilityMSG();
        Availability availability1 = new Availability();
        availability1.setDate(LocalDate.now());
        availability1.setStartTime(LocalTime.of(10, 0));
        availability1.setEndTime(LocalTime.of(12, 0));

        Availability availability2 = new Availability();
        availability2.setDate(LocalDate.now().plusDays(1));
        availability2.setStartTime(LocalTime.of(9, 0));
        availability2.setEndTime(LocalTime.of(11, 0));

        availabilityMSG.setAvailabilities(Arrays.asList(availability1, availability2));

        telegramMessageService.sendAgentSchedule(user, availabilityMSG);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(writeReadBot).execute(sendMessageCaptor.capture());

        SendMessage sendMessage = sendMessageCaptor.getValue();

        assertThat(sendMessage.getText()).contains("*Дата: " + LocalDate.now() + "*\n\n");
        assertThat(sendMessage.getText()).contains("10:00 - 12:00");
        assertThat(sendMessage.getText()).contains("*Дата: " + LocalDate.now().plusDays(1) + "*\n\n");
        assertThat(sendMessage.getText()).contains("9:00 - 11:00");
    }
}
