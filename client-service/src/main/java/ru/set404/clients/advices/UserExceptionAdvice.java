package ru.set404.clients.advices;

import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.set404.clients.exceptions.AgentNotFoundException;
import ru.set404.clients.exceptions.AgentServiceNotFoundException;
import ru.set404.clients.exceptions.TimeNotAvailableException;

import javax.management.ServiceNotFoundException;

@ControllerAdvice
public class UserExceptionAdvice {

    @ExceptionHandler(value = {TimeNotAvailableException.class})
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<Object> timeNotAvailableException(TimeNotAvailableException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ResponseBody
    @ExceptionHandler(AgentServiceNotFoundException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> serviceNotFoundHandler(AgentServiceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.NO_CONTENT);
    }

    @ResponseBody
    @ExceptionHandler(AgentNotFoundException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> agentNotFoundHandler(AgentNotFoundException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.NO_CONTENT);
    }
}
