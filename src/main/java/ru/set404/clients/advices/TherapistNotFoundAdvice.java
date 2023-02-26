package ru.set404.clients.advices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.set404.clients.exceptions.AppointmentNotFoundException;
import ru.set404.clients.exceptions.TherapistNotFoundException;

@ControllerAdvice
public class TherapistNotFoundAdvice {

    @ResponseBody
    @ExceptionHandler(TherapistNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String appointmentNotFoundHandler(TherapistNotFoundException ex) {
        return ex.getMessage();
    }
}
