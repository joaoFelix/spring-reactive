package com.reactivespring.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;

import lombok.RequiredArgsConstructor;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Component
@RequiredArgsConstructor
public class MoviesInfoRestClient {

    @Value("${restClient.moviesInfoUrl}") private String moviesInfoUrl;

    private final WebClient webClient;

    public Mono<MovieInfo> getMovieInfo(String movieId) {

        return webClient.get()
                        .uri(moviesInfoUrl + "/{id}", movieId)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                            HttpStatus httpStatus = clientResponse.statusCode();

                            return httpStatus.equals(HttpStatus.NOT_FOUND)
                                   ? Mono.error(new MoviesInfoClientException(String.format("No MovieInfo available with the id %s", movieId), httpStatus.value()))
                                   : clientResponse.bodyToMono(String.class)
                                                   .flatMap(errorMsg -> Mono.error(new MoviesInfoClientException(errorMsg, httpStatus.value())));
                        })
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                                                                                                .flatMap(errorMsg -> Mono.error(new MoviesInfoServerException(errorMsg))))
                        .bodyToMono(MovieInfo.class)
                        .retryWhen(RetryUtil.retrySpec())
                        .log();
    }

    public Flux<MovieInfo> getMovieInfoStream() {
        return webClient.get()
                        .uri(moviesInfoUrl + "/stream")
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                            HttpStatus httpStatus = clientResponse.statusCode();

                            return clientResponse.bodyToMono(String.class)
                                                 .flatMap(errorMsg -> Mono.error(new MoviesInfoClientException(errorMsg, httpStatus.value())));
                        })
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                                                                                                .flatMap(errorMsg -> Mono.error(new MoviesInfoServerException(errorMsg))))
                        .bodyToFlux(MovieInfo.class);
    }
}
