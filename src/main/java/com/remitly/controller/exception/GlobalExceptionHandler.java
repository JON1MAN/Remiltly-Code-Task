package com.remitly.controller.exception;

import com.remitly.controller.exception.swift_code.SwiftCodeAlreadyExistsException;
import com.remitly.controller.exception.swift_code.SwiftCodeNotFoundException;
import com.remitly.controller.exception.swift_code.SwiftCodeValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SwiftCodeNotFoundException.class)
    public ResponseEntity<ExceptionResponseDTO> handleSwiftCodeNotFoundException(SwiftCodeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(createResponse(HttpStatus.NOT_FOUND, ex));
    }

    @ExceptionHandler(SwiftCodeAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponseDTO> handleSwiftCodeAlreadyExistsException(SwiftCodeAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(createResponse(HttpStatus.CONFLICT, ex));
    }

    @ExceptionHandler(SwiftCodeValidationException.class)
    public ResponseEntity<ExceptionResponseDTO> handleSwiftCodeValidationException(SwiftCodeValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createResponse(HttpStatus.BAD_REQUEST, ex));
    }

    private ExceptionResponseDTO createResponse(HttpStatus status, Exception exception) {
        return ExceptionResponseDTO.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(exception.getMessage())
                .build();
    }
}
