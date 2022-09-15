package com.reactivespring.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reactivespring.client.MoviesInfoRestClient;
import com.reactivespring.client.ReviewsRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.Review;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
public class MoviesController {

    private final MoviesInfoRestClient moviesInfoRestClient;
    private final ReviewsRestClient reviewsRestClient;

    @GetMapping("/{movieId}")
    public Mono<Movie> getMovieById(@PathVariable("movieId") String movieId) {
        return moviesInfoRestClient.getMovieInfo(movieId)
                                   .flatMap(movieInfo -> {
                                       Mono<List<Review>> reviewsMono = reviewsRestClient.getReviews(movieId).collectList();

                                       return reviewsMono.map(reviews -> new Movie(movieInfo, reviews));
                                   });
    }
}
