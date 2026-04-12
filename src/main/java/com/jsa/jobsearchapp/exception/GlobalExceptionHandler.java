package com.jsa.jobsearchapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ErrorModel> handleUserAlreadyRegisteredException(UserAlreadyRegisteredException e) {
        return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorModel> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullDocumentException.class)
    public ResponseEntity<ErrorModel> handleNullDocumentException(NullDocumentException e) {
        return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnknownEnumValueException.class)
    public ResponseEntity<ErrorModel> handleUnknownEnumValueException(UnknownEnumValueException e) {
        return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullSideDetailsSetException.class)
    public ResponseEntity<ErrorModel> handleNullSideDetailsSetException(NullSideDetailsSetException e) {
        return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
