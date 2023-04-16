package ru.set404.clients.services;

import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.dto.AppointmentsForSiteDTO;
import ru.set404.clients.dto.AvailabilitiesDTO;
import ru.set404.clients.dto.ServiceDTO;
import ru.set404.clients.exceptions.*;
import ru.set404.clients.models.*;
import ru.set404.clients.repositories.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TherapistService {
    private final TherapistsRepository therapistsRepository;
    private final ServicesRepository servicesRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientsRepository clientsRepository;
    private final AvailabilityRepository availabilityRepository;

    private final ModelMapper modelMapper;


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

    public List<AppointmentDTO> findAllAppointments(Long therapistId) {

        List<AppointmentDTO> appointments = appointmentRepository.findAppointmentsForTherapist(therapistId)
                .stream().map(a -> modelMapper.map(a, AppointmentDTO.class)).toList();
        if (appointments.size() > 0) {
            return appointments;
        } else {
            throw new AppointmentNotFoundException(therapistId);
        }
    }

    public Appointment findAppointmentById(Long therapistId, Long appointmentId) {
        return appointmentRepository
                .findAppointmentForTherapistById(therapistId, appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(therapistId));
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

    public void deleteAppointment(Long therapistId, Long appointmentId) {
        LocalDate deletedDate = appointmentRepository.deleteAppointment(therapistId, appointmentId);
        List<LocalTime> appointedTime = appointmentRepository.findAppointmentsByDay(therapistId, deletedDate);

        if (availabilityRepository.findAvailableTimes(therapistId, deletedDate, appointedTime).size() > 0)
            availabilityRepository.markAvailabilityAs(therapistId, deletedDate, false);
    }

    public Therapist findTherapistById(Long therapistId) {
        return therapistsRepository
                .findTherapistById(therapistId)
                .orElseThrow(() -> new TherapistNotFoundException(therapistId));
    }

    public Therapist findTherapistByPhone(String phone) throws AuthException {
        return therapistsRepository
                .findTherapistByPhone(phone)
                .orElseThrow(() -> new AuthException(String.format("User with phone - %s not found", phone)));
    }

    public void updateTherapist(Therapist therapist) {
        therapistsRepository.updateTherapist(therapist);
    }

    public void addAvailableTime(Long therapistId, Availability availability) {
        List<LocalTime> appointedTime = appointmentRepository.findAppointmentsByDay(therapistId, availability.getDate());

        availabilityRepository.addOrUpdateAvailableTime(therapistId, availability, appointedTime);
    }

    public void addAvailableTime(Long therapistId, AvailabilitiesDTO availabilitiesDTO) {
        List<LocalTime> appointedTime = appointmentRepository.findAppointmentsByDay(therapistId,
                availabilitiesDTO.getStartTime().toLocalDate());

        availabilityRepository.addOrUpdateAvailableTime(therapistId, availabilitiesDTO, appointedTime);
    }

    public void deleteAvailableTime(Long therapistId, LocalDate date) {
        availabilityRepository.deleteAvailableTime(therapistId, date);
    }

    public void deleteTherapist(Long therapistId) {
        therapistsRepository.deleteTherapist(therapistId);
    }

    public Service findService(Long therapistId) {
        return servicesRepository.findServiceByTherapist(therapistId).orElseThrow(() -> new ServiceNotFoundException(therapistId));
    }

    public List<Client> findClients(Long therapistId) {
        List<Client> clients = clientsRepository.findClientsForTherapist(therapistId);
        if (clients.size() > 0) {
            return clients;
        } else {
            throw new ClientNotFoundException(therapistId);
        }
    }

    public void addOrUpdateService(Long therapistId, ServiceDTO service) {
        Service updatedService = modelMapper.map(service, Service.class);
        servicesRepository.addOrUpdateService(therapistId, updatedService);
    }

    public List<AppointmentsForSiteDTO> findAllAppointmentsDTO(Long therapistId) {
        List<AppointmentsForSiteDTO> appointments = appointmentRepository.findAppointmentsForTherapistSite(therapistId);
        if (appointments.size() > 0) {
            return appointments;
        } else {
            throw new AppointmentNotFoundException(therapistId);
        }
    }
}
