package com.reactivespring.exceptionhandler;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleRequestBodyError(WebExchangeBindException exception) {
        log.error("Caught WebExchangeBindException: ", exception);

        String errors = exception.getBindingResult()
                                  .getAllErrors()
                                  .stream()
                                  .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                  .sorted()
                                  .collect(Collectors.joining(", "));

        log.error("Validation Errors: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
