package com.jsa.jobsearchapp.exception;

public record ErrorResponse(String timestamp, int status, String message) {
}
