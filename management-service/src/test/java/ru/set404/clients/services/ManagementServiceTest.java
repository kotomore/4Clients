package ru.set404.clients.services;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.set404.clients.exceptions.AppointmentNotFoundException;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Schedule;
import ru.set404.clients.models.TimeSlot;
import ru.set404.clients.repositories.AppointmentRepository;
import ru.set404.clients.repositories.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ManagementServiceTest {

    @InjectMocks
    private ManagementService managementService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private static final String AGENT_ID = "123";
    private static final String APPOINTMENT_ID = "456";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindAllAppointments() {
        String agentId = "123";
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        List<Appointment> expectedAppointments = new ArrayList<>();
        expectedAppointments.add(appointment1);
        expectedAppointments.add(appointment2);

        when(appointmentRepository.findByAgentId(agentId)).thenReturn(expectedAppointments);

        List<Appointment> actualAppointments = managementService.findAllAppointments(agentId);

        Assert.assertEquals(expectedAppointments, actualAppointments);
    }

    @Test
    public void testFindAppointmentById() {
        String agentId = "123";
        String appointmentId = "456";
        Appointment expectedAppointment = new Appointment();

        Mockito.when(appointmentRepository.findByIdAndAgentId(appointmentId, agentId)).thenReturn(Optional.of(expectedAppointment));

        Appointment actualAppointment = managementService.findAppointmentById(agentId, appointmentId);

        Assert.assertEquals(expectedAppointment, actualAppointment);
    }

    @Test
    public void testFindAvailableDates() {
        String agentId = "123";
        LocalDate date = LocalDate.now();
        List<LocalDate> expectedDates = new ArrayList<>();
        expectedDates.add(LocalDate.now());
        expectedDates.add(LocalDate.now().plusDays(1));

        List<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0)));
        timeSlots.add(new TimeSlot(LocalTime.of(10, 0), LocalTime.of(11, 0)));
        timeSlots.add(new TimeSlot(LocalTime.of(11, 0), LocalTime.of(12, 0)));

        Mockito.when(scheduleRepository.findByAgentIdAndDateGreaterThanEqual(agentId, date)).thenReturn(
                Arrays.asList(new Schedule("1", agentId, date, timeSlots), new Schedule("id", agentId, date.plusDays(1), timeSlots)));

        List<LocalDate> actualDates = managementService.findAvailableDates(agentId, date);

        Assert.assertEquals(expectedDates, actualDates);
    }

    @Test
    public void testFindAvailableTimes() {
        String agentId = "123";
        LocalDate date = LocalDate.now();
        List<LocalTime> expectedTimes = Arrays.asList(LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0));

        Schedule schedule = new Schedule();
        List<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0)));
        timeSlots.add(new TimeSlot(LocalTime.of(10, 0), LocalTime.of(11, 0)));
        timeSlots.add(new TimeSlot(LocalTime.of(11, 0), LocalTime.of(12, 0)));
        schedule.setAvailableSlots(timeSlots);

        Mockito.when(scheduleRepository.findByAgentIdAndDate(agentId, date)).thenReturn(Optional.of(schedule));

        List<LocalTime> actualTimes = managementService.findAvailableTimes(agentId, date);

        Assert.assertEquals(expectedTimes, actualTimes);
    }
}
