package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.exceptions.TimeNotAvailableException;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Client;
import ru.set404.clients.repositories.AppointmentRepository;
import ru.set404.clients.repositories.AvailabilityRepository;
import ru.set404.clients.repositories.ClientsRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClientService {
    private final AppointmentRepository appointmentRepository;
    private final ClientsRepository clientsRepository;
    private final AvailabilityRepository availabilityRepository;

    public Appointment addAppointment(AppointmentDTO appointmentDTO) {
        Appointment appointment = appointmentDTO.toAppointment();

        List<LocalTime> availableTime = findAvailableTimes(appointmentDTO.getTherapistId(),
                appointmentDTO.getStartTime().toLocalDate());

        if (availabilityRepository.isTimeAvailable(appointment)
                && availableTime.contains(appointmentDTO.getStartTime().toLocalTime())) {

            Optional<Client> client = clientsRepository.findClientByPhoneNumber(appointment.getClient().getPhone());
            appointment.setClient(client.orElse(clientsRepository.createClient(appointment.getClient())));
            appointmentRepository.createAppointment(appointment);
            if (availableTime.size() < 1)
                availabilityRepository.markAvailabilityAs(appointment.getTherapistId(), appointment.getStartTime().toLocalDate(), true);
        } else {
            throw new TimeNotAvailableException();
        }
        return appointment;
    }

    public List<LocalTime> findAvailableTimes(Long therapistId, LocalDate date) {

        List<LocalTime> appointedTime = appointmentRepository.findAppointmentsByDay(therapistId, date);

        List<LocalTime> availableTimes = availabilityRepository.findAvailableTimes(therapistId, date, appointedTime);
        if (availableTimes.size() > 0)
            return availableTimes;
        else
            throw new TimeNotAvailableException();
    }

    public List<LocalDate> findAvailableDates(Long therapistId, LocalDate date) {
        List<LocalTime> appointedTime = appointmentRepository.findAppointmentsByDay(therapistId, date);

        List<LocalDate> availableDates = availabilityRepository.findAvailableDates(therapistId, date, appointedTime);
        if (availableDates.size() > 0)
            return availableDates;
        else
            throw new TimeNotAvailableException();
    }
}
