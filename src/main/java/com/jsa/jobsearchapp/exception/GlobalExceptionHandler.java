package com.jsa.jobsearchapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyRegisteredException(UserAlreadyRegisteredException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        e.getMessage(),
                        409,
                        System.currentTimeMillis()
                ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        e.getMessage(),
                        400,
                        System.currentTimeMillis()
                ));
    }

    @ExceptionHandler(NullDocumentException.class)
    public ResponseEntity<ErrorResponse> handleNullDocumentException(NullDocumentException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        e.getMessage(),
                        400,
                        System.currentTimeMillis()
                ));
    }

    @ExceptionHandler(UnknownEnumValueException.class)
    public ResponseEntity<ErrorResponse> handleUnknownEnumValueException(UnknownEnumValueException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        e.getMessage(),
                        400,
                        System.currentTimeMillis()
                ));
    }

    @ExceptionHandler(NullSideDetailsSetException.class)
    public ResponseEntity<ErrorResponse> handleNullSideDetailsSetException(NullSideDetailsSetException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        e.getMessage(),
                        400,
                        System.currentTimeMillis()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMissingBody(HttpMessageNotReadableException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        "Request body is missing or invalid",
                        400,
                        System.currentTimeMillis()
                ));
    }
}
