package com.reactivespring.exceptionhandler;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(final ServerWebExchange exchange, final Throwable ex) {
        log.error("Exception message is {}", ex.getMessage(), ex);

        ServerHttpResponse response = exchange.getResponse();

        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer errorMsg = dataBufferFactory.wrap(ex.getMessage().getBytes(StandardCharsets.UTF_8));

        if (ex instanceof ReviewDataException) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);

        } else if (ex instanceof ReviewNotFoundException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);

        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response.writeWith(Mono.just(errorMsg));
    }
}
