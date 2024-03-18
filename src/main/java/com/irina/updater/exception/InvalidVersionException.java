package com.irina.updater.exception;

public class InvalidVersionException extends Exception {
    public InvalidVersionException(String errorMessage) {
        super(errorMessage);
    }
}