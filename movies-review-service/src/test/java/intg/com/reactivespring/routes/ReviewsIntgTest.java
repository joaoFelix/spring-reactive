package com.reactivespring.routes;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewsIntgTest {

    private final String V1_MOVIE_REVIEWS_URL = "/v1/reviews";

    @Autowired WebTestClient webTestClient;

    @Autowired ReviewReactiveRepository reviewReactiveRepository;

    @BeforeEach
    void setUp() {
        List<Review> reviews = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie 1", 9.0),
                new Review("3", 2L, "Excellent Movie", 8.0)
        );

        reviewReactiveRepository.saveAll(reviews)
                                .blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        Review review = new Review(null, 1L, "Awesome Movie", 9.0);

        webTestClient.post()
                     .uri(V1_MOVIE_REVIEWS_URL)
                     .bodyValue(review)
                     .exchange()
                     .expectStatus()
                     .isCreated()
                     .expectBody(Review.class)
                     .consumeWith(reviewEntityExchangeResult -> {
                         Review review1 = reviewEntityExchangeResult.getResponseBody();

                         assertNotNull(review1);
                         assertNotNull(review1.getReviewId());
                     });
    }

    @Test
    void getReviews() {
        webTestClient.get()
                     .uri(V1_MOVIE_REVIEWS_URL)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Review.class)
                     .hasSize(3);
    }

    @Test
    void updateReview() {
        Review review = new Review("3", 2L, "Excellent Movie 2", 8.0);

        webTestClient.put()
                     .uri(V1_MOVIE_REVIEWS_URL + "/{id}", review.getReviewId())
                     .bodyValue(review)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBody(Review.class)
                     .consumeWith(reviewEntityExchangeResult -> {
                         Review updatedReview = reviewEntityExchangeResult.getResponseBody();

                         assertNotNull(updatedReview);
                         assertEquals(updatedReview.getReviewId(), review.getReviewId());
                         assertEquals(updatedReview.getComment(), review.getComment());
                     });
    }

    @Test
    void deleteReview() {
        webTestClient.delete()
                     .uri(V1_MOVIE_REVIEWS_URL + "/{id}", "3")
                     .exchange()
                     .expectStatus()
                     .isNoContent();
    }

    @Test
    void getReviewsForMovieInfo() {
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder
                             .path(V1_MOVIE_REVIEWS_URL)
                             .queryParam("movieInfoId", "1")
                             .build())
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Review.class)
                     .hasSize(2);
    }
}
