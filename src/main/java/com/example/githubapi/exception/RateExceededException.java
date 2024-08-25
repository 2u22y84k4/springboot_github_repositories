package com.example.githubapi.exception;

public class RateExceededException extends RuntimeException{
    public RateExceededException(String message) {
        super(message);
    }
}
