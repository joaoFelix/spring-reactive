package com.reactivespring.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntegrationTest {

    private final String V1_MOVIE_INFOS_URL = "/v1/movieinfos";
    @Autowired MovieInfoRepository movieInfoRepository;

    @Autowired WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var movieinfos = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );

        movieInfoRepository.saveAll(movieinfos)
                           .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll()
                           .block();
    }

    @Test
    void addMovieInfo() {
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins 1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient.post()
                     .uri(V1_MOVIE_INFOS_URL)
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .isCreated()
                     .expectBody(MovieInfo.class)
                     .consumeWith(movieInfoEntityExchangeResult -> {
                         MovieInfo movieInfo1 = movieInfoEntityExchangeResult.getResponseBody();

                         assertNotNull(movieInfo1);
                         assertNotNull(movieInfo1.getId());
                     });
    }

    @Test
    void getAllMovieInfos() {
        webTestClient.get()
                     .uri(V1_MOVIE_INFOS_URL)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(MovieInfo.class)
                     .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        String movieInfoId = "abc";

        webTestClient.get()
                     .uri(V1_MOVIE_INFOS_URL + "/{id}", movieInfoId)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBody()
                     .jsonPath("$.id").isEqualTo(movieInfoId)
                     .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void getMovieInfoById_returnsNotFound() {
        String movieInfoId = "def";

        webTestClient.get()
                     .uri(V1_MOVIE_INFOS_URL + "/{id}", movieInfoId)
                     .exchange()
                     .expectStatus()
                     .isNotFound();
    }

    @Test
    void updateMovieInfo() {
        String movieInfoId = "abc";
        String updatedName = "Dark Knight Rises 1";

        MovieInfo movieInfo = new MovieInfo("abc", updatedName, 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        webTestClient.put()
                     .uri(V1_MOVIE_INFOS_URL + "/{id}", movieInfoId)
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBody()
                     .jsonPath("$.id").isEqualTo(movieInfoId)
                     .jsonPath("$.name").isEqualTo(updatedName);
    }

    @Test
    void updateMovieInfo_returnsNotFound() {
        String movieInfoId = "def";

        MovieInfo movieInfo = new MovieInfo("def", "Dark Knight Rises 1", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        webTestClient.put()
                     .uri(V1_MOVIE_INFOS_URL + "/{id}", movieInfoId)
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .isNotFound();
    }

    @Test
    void deleteMovieInfo() {
        String movieInfoId = "abc";

        webTestClient.delete()
                     .uri(V1_MOVIE_INFOS_URL + "/{id}", movieInfoId)
                     .exchange()
                     .expectStatus()
                     .isNoContent();
    }
}