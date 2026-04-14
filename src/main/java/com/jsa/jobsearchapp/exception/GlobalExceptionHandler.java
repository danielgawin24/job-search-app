package com.jsa.jobsearchapp.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyRegistered(UserAlreadyRegisteredException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        409,
                        e.getMessage()
                ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        400,
                        e.getMessage()
                ));
    }

    @ExceptionHandler(NullDocumentException.class)
    public ResponseEntity<ErrorResponse> handleNullDocument(NullDocumentException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        400,
                        e.getMessage()
                ));
    }

    @ExceptionHandler(UnknownEnumValueException.class)
    public ResponseEntity<ErrorResponse> handleUnknownEnumValue(UnknownEnumValueException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        400,
                        e.getMessage()
                ));
    }

    @ExceptionHandler(NullSideDetailsSetException.class)
    public ResponseEntity<ErrorResponse> handleNullSideDetailsSet(NullSideDetailsSetException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        400,
                        e.getMessage()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMissingBody(HttpMessageNotReadableException ignored) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        400,
                        "Request body is missing or invalid"
                ));
    }

    @ExceptionHandler(InvalidRequestBodyException.class)
    public ResponseEntity<ErrorResponse> handleMissingBody(InvalidRequestBodyException ignored) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        400,
                        "Request body is missing or invalid"
                ));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        404,
                        e.getMessage()
                ));
    }
}
