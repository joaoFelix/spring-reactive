package com.learnreactiveprogramming.service;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                   .log();
    }

    public Mono<String> namesMono() {
        return Mono.just("alex")
                   .log();
    }

    public Flux<String> namesFluxMap(int length) {
        return namesFlux()
                .map(String::toUpperCase)
                .filter(s -> s.length() > length)
                .map(s -> s.length() + "-" + s)
                .log();
    }

    public Flux<String> namesFluxImmutability() {
        Flux<String> namesFlux = namesFlux();
        namesFlux.map(String::toUpperCase);

        return namesFlux;
    }

    public Flux<String> namesFluxFlatMap(int length) {
        return namesFlux()
                .map(String::toUpperCase)
                .filter(s -> s.length() > length)
                .flatMap(this::splitString)
                .log();
    }

    public Flux<String> namesFluxFlatMapAsync(int length) {
        return namesFlux()
                .map(String::toUpperCase)
                .filter(s -> s.length() > length)
                .flatMap(this::splitStringWithDelay)
                .log();
    }

    public Flux<String> namesFluxConcatMap(int length) {
        return namesFlux()
                .map(String::toUpperCase)
                .filter(s -> s.length() > length)
                .concatMap(this::splitStringWithDelay)
                .log();
    }

    public Mono<List<String>> namesMonoFlatMap(int length) {
        return Mono.just("alex")
                   .map(String::toUpperCase)
                   .filter(s -> s.length() > length)
                   .flatMap(this::splitStringMono)
                   .log();
    }

    public Flux<String> namesMonoFlatMapMany(int length) {
        return Mono.just("alex")
                   .map(String::toUpperCase)
                   .filter(s -> s.length() > length)
                   .flatMapMany(this::splitString)
                   .log();
    }

    public Flux<String> namesFluxTransform(int length) {
        Function<Flux<String>, Flux<String>> filterMap = stringFlux -> stringFlux.map(String::toUpperCase)
                                                                                 .filter(s -> s.length() > length);

        return namesFlux()
                .transform(filterMap)
                .flatMap(this::splitString)
                .log();
    }

    public Flux<String> namesFluxTransformDefaultIfEmpty(int length) {
        return namesFluxTransform(length)
                .defaultIfEmpty("Default")
                .log();
    }

    public Flux<String> namesFluxTransformSwitchIfEmpty(int length) {
        return namesFluxTransform(length)
                .switchIfEmpty(Flux.just("Switch"))
                .log();
    }

    public Flux<String> exploreConcat() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");

        return Flux.concat(abcFlux, defFlux)
                   .log();
    }

    public Flux<String> exploreConcatWith() {
        Mono<String> aFlux = Mono.just("A");
        Mono<String> bFlux = Mono.just("B");

        return aFlux.concatWith(bFlux);
    }

    public Flux<String> exploreMerge() {
        Flux<String> abcFlux = Flux.just("A", "B", "C")
                                   .delayElements(Duration.ofMillis(100));

        Flux<String> defFlux = Flux.just("D", "E", "F")
                                   .delayElements(Duration.ofMillis(125));

        return Flux.merge(abcFlux, defFlux)
                   .log();
    }

    public Flux<String> exploreMergeWith() {
        Mono<String> aFlux = Mono.just("A")
                                 .delayElement(Duration.ofMillis(125));

        Mono<String> bFlux = Mono.just("B")
                                 .delayElement(Duration.ofMillis(100));

        return aFlux.mergeWith(bFlux)
                    .log();
    }

    public Flux<String> exploreMergeSequential() {
        Flux<String> abcFlux = Flux.just("A", "B", "C")
                                   .delayElements(Duration.ofMillis(100));

        Flux<String> defFlux = Flux.just("D", "E", "F")
                                   .delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(abcFlux, defFlux)
                   .log();
    }

    public Flux<String> exploreZip() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");

        Flux<String> defFlux = Flux.just("D", "E", "F");

        return Flux.zip(abcFlux, defFlux, (s1, s2) -> s1 + s2)
                   .log();
    }

    public Flux<String> exploreZipMap() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");

        Flux<String> _123Flux = Flux.just("1", "2", "3");
        Flux<String> _456Flux = Flux.just("4", "5", "6");

        return Flux.zip(abcFlux, defFlux, _123Flux, _456Flux)
                   .map(tuple -> tuple.getT1() + tuple.getT2() + tuple.getT3() + tuple.getT4())
                   .log();
    }

    public Flux<String> exploreZipWith() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");

        Flux<String> defFlux = Flux.just("D", "E", "F");

        return abcFlux.zipWith(defFlux, (s1, s2) -> s1 + s2)
                      .log();
    }

    public Mono<String> exploreZipWithMono() {
        Mono<String> aMono = Mono.just("A");

        Mono<String> bMono = Mono.just("B");

        return aMono.zipWith(bMono)
                    .map(tuple -> tuple.getT1() + tuple.getT2())
                    .log();
    }

    public Flux<String> splitString(String str) {
        return Flux.fromArray(str.split(""));
    }

    public Flux<String> splitStringWithDelay(String str) {
        int millis = new Random().nextInt(1000);

        return Flux.fromArray(str.split(""))
                   .delayElements(Duration.ofMillis(millis));
    }

    public Mono<List<String>> splitStringMono(String str) {
        return Mono.just(List.of(str.split("")));
    }


    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

        fluxAndMonoGeneratorService.namesFlux()
                                   .subscribe(name -> System.out.println("Flux Name is : " + name));

        fluxAndMonoGeneratorService.namesMono()
                                   .subscribe(name -> System.out.println("Mono Name is : " + name));
    }
}