package com.jsa.jobsearchapp.exception;

public record ErrorResponse(String message, int status, long timestamp) {
}
