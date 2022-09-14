package com.reactivespring.handler;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReviewHandler {

    private final ReviewReactiveRepository reviewReactiveRepository;

    public Mono<ServerResponse> addReview(final ServerRequest request) {
        return request.bodyToMono(Review.class)
                      .flatMap(reviewReactiveRepository::save)
                      .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        Optional<String> movieInfoId = request.queryParam("movieInfoId");

        Flux<Review> reviews = movieInfoId.isPresent()
                               ? reviewReactiveRepository.findAllByMovieInfoId(Long.valueOf(movieInfoId.get()))
                               : reviewReactiveRepository.findAll();

        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> updateReview(final ServerRequest request) {
        String reviewId = request.pathVariable("id");
        return reviewReactiveRepository.findById(reviewId)
                                       .flatMap(review -> request.bodyToMono(Review.class)
                                                                 .map(reqReview -> {
                                                                     review.setComment(reqReview.getComment());
                                                                     review.setRating(reqReview.getRating());
                                                                     return review;
                                                                 })
                                                                 .flatMap(reviewReactiveRepository::save)
                                                                 .flatMap(ServerResponse.ok()::bodyValue));


    }

    public Mono<ServerResponse> deleteReview(final ServerRequest request) {
        String reviewId = request.pathVariable("id");
        return reviewReactiveRepository.findById(reviewId)
                                       .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                                       .then(ServerResponse.noContent().build());
    }
}
