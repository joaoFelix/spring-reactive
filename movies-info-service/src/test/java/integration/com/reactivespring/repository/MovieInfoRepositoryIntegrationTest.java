package com.reactivespring.repository;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import com.reactivespring.domain.MovieInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAll() {
        Flux<MovieInfo> flux = movieInfoRepository.findAll()
                                                  .log();

        StepVerifier.create(flux)
                    .expectNextCount(3)
                    .verifyComplete();
    }

    @Test
    void findById() {
        Mono<MovieInfo> mono = movieInfoRepository.findById("abc")
                                                  .log();

        StepVerifier.create(mono)
                    .assertNext(movieInfo -> {
                        assertEquals("Dark Knight Rises", movieInfo.getName());
                    })
                    .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins 1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        Mono<MovieInfo> mono = movieInfoRepository.save(movieInfo);

        StepVerifier.create(mono)
                    .assertNext(mi -> {
                        assertNotNull(mi.getId());
                        assertEquals("Batman Begins 1", mi.getName());
                    })
                    .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        int year = 2020;

        MovieInfo movieInfo = movieInfoRepository.findById("abc")
                                                 .log()
                                                 .block();
        movieInfo.setYear(year);

        Mono<MovieInfo> mono = movieInfoRepository.save(movieInfo);

        StepVerifier.create(mono)
                    .assertNext(mi -> {
                        assertEquals(year, mi.getYear());
                    })
                    .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        movieInfoRepository.deleteById("abc")
                           .log()
                           .block();

        Flux<MovieInfo> flux = movieInfoRepository.findAll()
                                                 .log();

        StepVerifier.create(flux)
                    .expectNextCount(2)
                    .verifyComplete();
    }

    @Test
    void findByYear() {
        Flux<MovieInfo> flux = movieInfoRepository.findByYear(2005)
                                                  .log();

        StepVerifier.create(flux)
                    .expectNextCount(1)
                    .verifyComplete();
    }

    @Test
    void findFirstByName() {
        Mono<MovieInfo> mono = movieInfoRepository.findFirstByName("Batman Begins")
                                                  .log();

        StepVerifier.create(mono)
                    .expectNextCount(1)
                    .verifyComplete();
    }
}