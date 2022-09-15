package com.reactivespring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import com.reactivespring.domain.MovieInfo;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class MoviesInfoRestClient {

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    private final WebClient webClient;

    public Mono<MovieInfo> getMovieInfo(String movieId){
        return webClient.get()
                .uri(moviesInfoUrl + "/{id}", movieId)
                .retrieve()
                .bodyToMono(MovieInfo.class)
                .log();
    }
}
