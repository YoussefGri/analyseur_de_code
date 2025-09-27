package com.example.agencehotelrest.exceptions;

public class AgenceNotFoundException extends RuntimeException {

    public AgenceNotFoundException() {
        super();
    }

    public AgenceNotFoundException(String message) {
        super(message);
    }
}
