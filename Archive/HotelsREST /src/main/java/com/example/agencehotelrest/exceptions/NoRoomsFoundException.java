package com.example.agencehotelrest.exceptions;

public class NoRoomsFoundException extends RuntimeException {
    public NoRoomsFoundException(String message) {
        super(message);
    }
}
