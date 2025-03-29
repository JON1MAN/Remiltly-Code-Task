package com.remitly.controller.exception.swift_code;

public class SwiftCodeAlreadyExistsException extends RuntimeException {
    public SwiftCodeAlreadyExistsException(String message) {
        super(message);
    }
}
