package com.sporty.jackpot.service;

/**
 * Thrown when a requested resource (e.g. a bet's contribution) does not exist.
 * Mapped to HTTP 404 by the global exception handler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
