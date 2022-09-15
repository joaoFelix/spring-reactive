package com.reactivespring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.reactivespring.domain.Review;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class ReviewsRestClient {

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    private final WebClient webClient;

    public Flux<Review> getReviews(String movieId){
        String reviewsUri = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                                                 .queryParam("movieInfoId", movieId)
                                                 .buildAndExpand()
                                                 .toUriString();

        return webClient.get()
                        .uri(reviewsUri)
                        .retrieve()
                        .bodyToFlux(Review.class)
                        .log();
    }
}
