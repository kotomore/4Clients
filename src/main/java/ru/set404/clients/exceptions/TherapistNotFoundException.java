package ru.set404.clients.exceptions;

public class TherapistNotFoundException extends RuntimeException{
    public TherapistNotFoundException(Long id) {
        super("Could not find therapist for id - " + id);
    }

}
