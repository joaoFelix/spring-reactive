package com.reactivespring.routes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
class ReviewsUnitTest {

    private final static String V1_MOVIE_REVIEWS_URL = "/v1/reviews";

    @MockBean ReviewReactiveRepository reviewReactiveRepository;

    @Autowired WebTestClient webTestClient;

    private final Review review1 = new Review("1", 1L, "Awesome Movie", 9.0);
    private final Review review2 = new Review("2", 2L, "Awesome Movie 2", 9.0);

    @Test
    void addReview() {
        Review review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(review))
                .thenReturn(Mono.just(review1));

        webTestClient.post()
                     .uri(V1_MOVIE_REVIEWS_URL)
                     .bodyValue(review)
                     .exchange()
                     .expectStatus()
                     .isCreated()
                     .expectBody(Review.class)
                     .isEqualTo(review1);
    }

    @Test
    void addReview_validatesReview() {
        Review review = new Review(null, null, "Awesome Movie", -1.0);

        webTestClient.post()
                     .uri(V1_MOVIE_REVIEWS_URL)
                     .bodyValue(review)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectBody(String.class)
                     .isEqualTo("rating.movieInfoId: must not be null, rating.negative : please pass a non-negative value");
    }


    @Test
    void getReviews() {
        when(reviewReactiveRepository.findAll())
                .thenReturn(Flux.just(review1, review2));

        webTestClient.get()
                     .uri(V1_MOVIE_REVIEWS_URL)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Review.class)
                     .hasSize(2);
    }

    @Test
    void updateReview() {
        Review review = new Review(review2.getReviewId(), review2.getMovieInfoId(), "Excellent Movie 2", 8.0);

        when(reviewReactiveRepository.findById(review.getReviewId()))
                .thenReturn(Mono.just(review2));

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(review));

        webTestClient.put()
                     .uri(V1_MOVIE_REVIEWS_URL + "/{id}", review.getReviewId())
                     .bodyValue(review)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBody(Review.class)
                     .isEqualTo(review);
    }

    @Test
    void deleteReview() {
        when(reviewReactiveRepository.findById(review1.getReviewId()))
                .thenReturn(Mono.just(review1));

        when(reviewReactiveRepository.deleteById(review1.getReviewId()))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                     .uri(V1_MOVIE_REVIEWS_URL + "/{id}", review1.getReviewId())
                     .exchange()
                     .expectStatus()
                     .isNoContent();
    }

    @Test
    void getReviewsForMovieInfo() {
        when(reviewReactiveRepository.findAllByMovieInfoId(review1.getMovieInfoId()))
                .thenReturn(Flux.just(review1));

        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder
                             .path(V1_MOVIE_REVIEWS_URL)
                             .queryParam("movieInfoId", review1.getMovieInfoId())
                             .build())
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Review.class)
                     .hasSize(1);
    }
}
