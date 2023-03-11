package ru.set404.clients.advices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import ru.set404.clients.exceptions.TimeNotAvailableException;
import ru.set404.clients.exceptions.UserAlreadyExistException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object>  authExceptionHandler(AuthException ex) {
        return new ResponseEntity<>(new ErrorMessage("Authorization error. " + ex.getMessage()), new HttpHeaders(), HttpStatus.FORBIDDEN);

    }

    @ExceptionHandler(value = {JwtException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> requestNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {TimeNotAvailableException.class})
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<Object> requestNotSupportedException(TimeNotAvailableException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = {UserAlreadyExistException.class})
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<Object> requestNotSupportedException(UserAlreadyExistException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<Object> requestNotSupportedException(MethodArgumentNotValidException ex) throws JsonProcessingException {

        StringBuilder builder = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            builder.append(fieldName).append(" - ").append(errorMessage).append(". ");
        });
        return new ResponseEntity<>(new ErrorMessage(builder.toString()), new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE);
    }
}
