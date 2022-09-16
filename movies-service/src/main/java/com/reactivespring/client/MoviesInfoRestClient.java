package com.reactivespring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor public class MoviesInfoRestClient {

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
                        .bodyToMono(MovieInfo.class).log();
    }
}
