package com.reactivespring.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MoviesInfoController {

    private final MovieInfoService movieInfoService;

    private Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().latest();

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return movieInfoService.addMovie(movieInfo)
                               .doOnNext(addedMovieInfo -> movieInfoSink.tryEmitNext(addedMovieInfo))
                               .log();
    }

    @GetMapping(value = "/movieinfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> replayAllAddedMovies() {
        return movieInfoSink.asFlux();
    }


    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value = "year", required = false) Integer year) {
        return year != null
               ? movieInfoService.getMovieInfosByYear(year).log()
               : movieInfoService.getAllMovieInfos().log();
    }

    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {
        return movieInfoService.getMovieInfoById(id)
                               .map(ResponseEntity.ok()::body)
                               .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                               .log();
    }

    @PutMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@PathVariable String id, @RequestBody MovieInfo updatedMovieInfo) {
        return movieInfoService.updateMovieInfo(id, updatedMovieInfo)
                               .map(ResponseEntity.ok()::body)
                               .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                               .log();
    }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id) {
        return movieInfoService.deleteMovieInfo(id)
                               .log();
    }
}
