package com.reactivespring.globalerrorhandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.reactivespring.exception.MoviesInfoClientException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(MoviesInfoClientException.class)
    public ResponseEntity<String> handleMoviesInfoClientExceptions(MoviesInfoClientException exception) {
        String exceptionMessage = exception.getMessage();

        log.error("MoviesInfoClientException caught: {}", exceptionMessage);
        return ResponseEntity.status(exception.getStatusCode()).body(exceptionMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeExceptions(RuntimeException exception) {
        String exceptionMessage = exception.getMessage();

        log.error("RuntimeException caught: {}", exceptionMessage);
        return ResponseEntity.internalServerError().body(exceptionMessage);
    }
}
