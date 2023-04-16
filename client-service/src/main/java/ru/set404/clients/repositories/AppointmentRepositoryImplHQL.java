package ru.set404.clients.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.set404.clients.models.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
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
}
