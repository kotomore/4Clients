package ru.kotomore.authservice.advices;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.security.auth.message.AuthException;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.kotomore.authservice.exceptions.UserAlreadyExistException;

@ControllerAdvice
public class AuthExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Object> authExceptionHandler(AuthException ex) {
        return new ResponseEntity<>(new ErrorMessage("Authorization error. " + ex.getMessage()), new HttpHeaders(), HttpStatus.UNAUTHORIZED);

    }

    @ExceptionHandler(value = {ExpiredJwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {UserAlreadyExistException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> userAlreadyExistException(UserAlreadyExistException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
