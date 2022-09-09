package com.reactivespring.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerTest {

    private final String V1_MOVIE_INFOS_URL = "/v1/movieinfos";

    @Autowired WebTestClient webTestClient;

    @MockBean MovieInfoService movieInfoService;

    @Test
    void getAllMoviesInfo() {
        when(movieInfoService.getAllMovieInfos()).thenReturn(Flux.fromIterable(List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        )));

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

        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoService.getMovieInfoById(movieInfo.getId()))
                .thenReturn(Mono.just(movieInfo));

        webTestClient.get()
                     .uri(V1_MOVIE_INFOS_URL + "/{id}", movieInfo.getId())
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBody()
                     .jsonPath("$.id").isEqualTo(movieInfo.getId())
                     .jsonPath("$.name").isEqualTo(movieInfo.getName());
    }

    @Test
    void addMovieInfo() {
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins 1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        MovieInfo expectedMovieInfo = new MovieInfo("newMovieId", "Batman Begins 1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(movieInfoService.addMovie(movieInfo))
                .thenReturn(Mono.just(expectedMovieInfo));

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
                         assertEquals(expectedMovieInfo, movieInfo1);
                     });
    }

    @Test
    void updateMovieInfo() {
        String updatedName = "Dark Knight Rises 1";

        MovieInfo movieInfo = new MovieInfo("abc", updatedName, 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoService.updateMovieInfo(movieInfo.getId(), movieInfo))
                .thenReturn(Mono.just(movieInfo));

        webTestClient.put()
                     .uri(V1_MOVIE_INFOS_URL + "/{id}", movieInfo.getId())
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBody()
                     .jsonPath("$.id").isEqualTo(movieInfo.getId())
                     .jsonPath("$.name").isEqualTo(updatedName);
    }

    @Test
    void deleteMovieInfo() {
        String movieInfoId = "abc";

        when(movieInfoService.deleteMovieInfo(movieInfoId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                     .uri(V1_MOVIE_INFOS_URL + "/{id}", movieInfoId)
                     .exchange()
                     .expectStatus()
                     .isNoContent();
    }
}