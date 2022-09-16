package com.reactivespring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class ReviewsRestClient {

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    private final WebClient webClient;

    public Flux<Review> getReviews(String movieId) {
        String reviewsUri = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                                                .queryParam("movieInfoId", movieId)
                                                .buildAndExpand()
                                                .toUriString();

        return webClient.get()
                        .uri(reviewsUri)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                            HttpStatus httpStatus = clientResponse.statusCode();

                            return httpStatus.equals(HttpStatus.NOT_FOUND)
                                   ? Mono.empty()
                                   : clientResponse.bodyToMono(String.class)
                                                   .flatMap(errorMsg -> Mono.error(new ReviewsClientException(errorMsg)));
                        })
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                                                                                                .flatMap(errorMsg -> Mono.error(new ReviewsServerException(errorMsg))))
                        .bodyToFlux(Review.class)
                        .log();
    }
}
