package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.NOT_FOUND;
import static org.springframework.cloud.contract.spec.internal.MediaTypes.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(properties = {
        "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
        "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
})
class MoviesControllerIntgTest {

    static final String MOVIES_URL = "/v1/movies";
    static final String MOVIE_INFOS_URL = "/v1/movieinfos";
    private final String MOVIE_REVIEWS_URL = "/v1/reviews";

    @Autowired WebTestClient webTestClient;

    @Test
    void getMovieById() {
        String movieId = "movieId";

        stubFor(get(urlEqualTo(MOVIE_INFOS_URL + "/" + movieId))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                            .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo(MOVIE_REVIEWS_URL))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                            .withBodyFile("reviews.json")));

        webTestClient.get()
                     .uri(MOVIES_URL + "/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBody(Movie.class)
                     .consumeWith(movieEntityExchangeResult -> {
                         Movie movie = movieEntityExchangeResult.getResponseBody();

                         assertEquals(2, movie.getReviewList().size());
                         assertEquals("Batman Begins", movie.getMovieInfo().getName());
                     });
    }

    @Test
    void getMovieById_movieInfo_404() {
        String movieId = "movieId";
        UrlPattern getMovieInfoUrlPattern = urlEqualTo(MOVIE_INFOS_URL + "/" + movieId);

        stubFor(get(getMovieInfoUrlPattern)
                        .willReturn(aResponse().withStatus(NOT_FOUND)));

        webTestClient.get()
                     .uri(MOVIES_URL + "/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .is4xxClientError()
                     .expectBody(String.class)
                     .isEqualTo("No MovieInfo available with the id " + movieId);

        WireMock.verify(1, getRequestedFor(getMovieInfoUrlPattern));
    }

    @Test
    void getMovieById_reviews_404() {
        String movieId = "movieId";
        UrlPathPattern getReviewsUrlPattern = urlPathEqualTo(MOVIE_REVIEWS_URL);

        stubFor(get(urlEqualTo(MOVIE_INFOS_URL + "/" + movieId))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                            .withBodyFile("movieinfo.json")));


        stubFor(get(getReviewsUrlPattern)
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                            .withBody("")));

        webTestClient.get()
                     .uri(MOVIES_URL + "/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBody(Movie.class)
                     .consumeWith(movieEntityExchangeResult -> {
                         Movie movie = movieEntityExchangeResult.getResponseBody();

                         assertEquals(0, movie.getReviewList().size());
                         assertEquals("Batman Begins", movie.getMovieInfo().getName());
                     });

        WireMock.verify(1, getRequestedFor(getReviewsUrlPattern));
    }

    @Test
    void getMovieById_movieInfo_500() {
        String movieId = "movieId";
        String expectedErrorMessage = "Movie Info Service Unavailable";

        UrlPattern getMovieInfoUrlPattern = urlEqualTo(MOVIE_INFOS_URL + "/" + movieId);

        stubFor(get(getMovieInfoUrlPattern)
                        .willReturn(aResponse()
                                            .withStatus(INTERNAL_SERVER_ERROR)
                                            .withBody(expectedErrorMessage)));

        webTestClient.get()
                     .uri(MOVIES_URL + "/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .is5xxServerError()
                     .expectBody(String.class)
                     .isEqualTo(expectedErrorMessage);

        WireMock.verify(4, getRequestedFor(getMovieInfoUrlPattern));
    }

    @Test
    void getMovieById_reviews_500() {
        String movieId = "movieId";
        UrlPathPattern getReviewsUrlPattern = urlPathEqualTo(MOVIE_REVIEWS_URL);

        stubFor(get(urlEqualTo(MOVIE_INFOS_URL + "/" + movieId))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                            .withBodyFile("movieinfo.json")));

        String expectedErrorMessage = "Reviews Service Unavailable";

        stubFor(get(getReviewsUrlPattern)
                        .willReturn(aResponse()
                                            .withStatus(INTERNAL_SERVER_ERROR)
                                            .withBody(expectedErrorMessage)));

        webTestClient.get()
                     .uri(MOVIES_URL + "/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .is5xxServerError()
                     .expectBody(String.class)
                     .isEqualTo(expectedErrorMessage);

        WireMock.verify(4, getRequestedFor(getReviewsUrlPattern));
    }
}
