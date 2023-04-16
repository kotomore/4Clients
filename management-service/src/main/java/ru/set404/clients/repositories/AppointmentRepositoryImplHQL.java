package ru.set404.clients.repositories;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.set404.clients.dto.AppointmentsForSiteDTO;
import ru.set404.clients.models.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AppointmentRepositoryImplHQL implements AppointmentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Appointment> findAppointmentsForTherapist(Long therapistId) {
        String hql = "SELECT a FROM Appointments a " +
                "WHERE a.therapistId = :therapistId";
        TypedQuery<Appointment> query = entityManager.createQuery(hql, Appointment.class);
        query.setParameter("therapistId", therapistId);
        return query.getResultList();
    }

    public List<AppointmentsForSiteDTO> findAppointmentsForTherapistSite(Long therapistId) {
        List<AppointmentsForSiteDTO> appointments = new ArrayList<>();
        String hql = "SELECT new AppointmentsForSiteDTO(a.appointmentId, c.name, c.phone, a.startTime, s.duration) "
                + "FROM Appointments a "
                + "JOIN Clients c "
                + "JOIN Services s "
                + "WHERE a.therapistId = :therapistId and s.serviceId = a.serviceId and a.client.id = c.id";
        try {
            appointments = entityManager.createQuery(hql, AppointmentsForSiteDTO.class)
                    .setParameter("therapistId", therapistId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appointments;
    }

    @Override
    public Optional<Appointment> findAppointmentForTherapistById(Long therapistId, Long appointmentId) {
        try {
            String hql = "SELECT a FROM Appointments a "
                    + "WHERE a.therapistId = :therapistId AND a.appointmentId = :appointmentId";
            Appointment appointment = entityManager.createQuery(hql, Appointment.class)
                    .setParameter("therapistId", therapistId)
                    .setParameter("appointmentId", appointmentId)
                    .getSingleResult();
            return Optional.of(appointment);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void createAppointment(Appointment appointment) {
        entityManager.persist(appointment);
    }

    @Override
    public List<LocalTime> findAppointmentsByDay(Long therapistId, LocalDate date) {
        String hql = "SELECT a.startTime FROM Appointments a "
                + "WHERE a.therapistId = :therapistId "
                + "AND a.startTime between :date and :dateEnd";
        return entityManager.createQuery(hql, LocalTime.class)
                .setParameter("therapistId", therapistId)
                .setParameter("date", date.atStartOfDay())
                .setParameter("dateEnd", date.plusDays(1).atStartOfDay().minusMinutes(1))
                .getResultList();
    }

    @Override
    public LocalDate deleteAppointment(Long therapistId, Long appointmentId) {
        LocalDate date = null;
        String hql = "SELECT a.startTime FROM Appointments a "
                + "WHERE a.appointmentId = :appointmentId "
                + "AND a.therapistId = :therapistId";
        TypedQuery<LocalDateTime> query = entityManager.createQuery(hql, LocalDateTime.class)
                .setParameter("appointmentId", appointmentId)
                .setParameter("therapistId", therapistId);
        List<LocalDateTime> results = query.getResultList();
        if (!results.isEmpty()) {
            date = results.get(0).toLocalDate();
            hql = "DELETE FROM Appointments a "
                    + "WHERE a.appointmentId = :appointmentId "
                    + "AND a.therapistId = :therapistId";
            entityManager.createQuery(hql)
                    .setParameter("appointmentId", appointmentId)
                    .setParameter("therapistId", therapistId)
                    .executeUpdate();
        }
        return date;
    }
}
