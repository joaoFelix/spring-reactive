package com.reactivespring.handler;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewHandler {

    private final Validator validator;
    private final ReviewReactiveRepository reviewReactiveRepository;

    public Mono<ServerResponse> addReview(final ServerRequest request) {
        return request.bodyToMono(Review.class)
                      .doOnNext(this::validateReview)
                      .flatMap(reviewReactiveRepository::save)
                      .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validateReview(final Review review) {
        Set<ConstraintViolation<Review>> constraintViolations = validator.validate(review);
        log.debug("constraintViolations: {}", constraintViolations);

        if (!constraintViolations.isEmpty()) {
            String errorMsg = constraintViolations.stream()
                                                  .map(ConstraintViolation::getMessage)
                                                  .sorted()
                                                  .collect(Collectors.joining(", "));

            throw new ReviewDataException(errorMsg);
        }

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

        Mono<Review> existingReview = reviewReactiveRepository.findById(reviewId);
                // Approach 1 to return a 404 - Not Found
                //.switchIfEmpty(Mono.error(new ReviewNotFoundException(String.format("Review with id %s not found", reviewId))));

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                                          .map(reqReview -> {
                                              review.setComment(reqReview.getComment());
                                              review.setRating(reqReview.getRating());
                                              return review;
                                          })
                                          .flatMap(reviewReactiveRepository::save)
                                          .flatMap(ServerResponse.ok()::bodyValue))
                .switchIfEmpty(ServerResponse.notFound().build()); // Approach 2 to return a 404 - Not Found


    }

    public Mono<ServerResponse> deleteReview(final ServerRequest request) {
        String reviewId = request.pathVariable("id");
        return reviewReactiveRepository.findById(reviewId)
                                       .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                                       .then(ServerResponse.noContent().build());
    }
}
