package ru.kotomore.taptimes.advices;

import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.kotomore.taptimes.exceptions.AgentNotFoundException;
import ru.kotomore.taptimes.exceptions.AgentServiceNotFoundException;
import ru.kotomore.taptimes.exceptions.TimeNotAvailableException;

@ControllerAdvice
public class UserExceptionAdvice {

    @ExceptionHandler(value = {TimeNotAvailableException.class})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> timeNotAvailableException(TimeNotAvailableException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.OK);
    }

    @ResponseBody
    @ExceptionHandler(AgentServiceNotFoundException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> serviceNotFoundHandler(AgentServiceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.OK);
    }

    @ResponseBody
    @ExceptionHandler(AgentNotFoundException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> agentNotFoundHandler(AgentNotFoundException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.OK);
    }
}
