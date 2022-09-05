package com.learnreactiveprogramming.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFlux())
                    .expectNext("alex", "ben", "chloe")
                    //.expectNextCount(3)
                    .verifyComplete();
    }

    @Test
    void namesMono() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesMono())
                    .expectNext("alex")
                    .verifyComplete();
    }

    @Test
    void namesFluxMap() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxMap(3))
                    .expectNext("4-ALEX", "5-CHLOE")
                    .verifyComplete();
    }

    @Test
    void namesFluxImmutability() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxImmutability())
                    .expectNext("alex", "ben", "chloe")
                    .verifyComplete();
    }

    @Test
    void namesFluxFlatMap() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxFlatMap(3))
                    .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                    .verifyComplete();
    }

    @Test
    void namesFluxFlatMapAsync() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxFlatMapAsync(3))
                    .expectNextCount(9)
                    .verifyComplete();
    }

    @Test
    void namesFluxConcatMap() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxConcatMap(3))
                    .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                    .verifyComplete();
    }

    @Test
    void namesMonoFlatMap() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesMonoFlatMap(3))
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();
    }

    @Test
    void namesMonoFlatMapMany() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesMonoFlatMapMany(3))
                    .expectNext("A", "L", "E", "X")
                    .verifyComplete();
    }

    @Test
    void namesFluxTransform() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxTransform(3))
                    .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                    .verifyComplete();
    }

    @Test
    void namesFluxTransformDefaultIfEmpty() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxTransformDefaultIfEmpty(6))
                    .expectNext("Default")
                    .verifyComplete();
    }

    @Test
    void namesFluxTransformSwitchIfEmpty() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxTransformSwitchIfEmpty(6))
                    .expectNext("Switch")
                    .verifyComplete();
    }

    @Test
    void exploreConcat() {
        StepVerifier.create(fluxAndMonoGeneratorService.exploreConcat())
                    .expectNext("A", "B", "C", "D", "E", "F")
                    .verifyComplete();
    }

    @Test
    void exploreConcatWith() {
        StepVerifier.create(fluxAndMonoGeneratorService.exploreConcatWith())
                    .expectNext("A", "B")
                    .verifyComplete();
    }

    @Test
    void exploreMerge() {
        StepVerifier.create(fluxAndMonoGeneratorService.exploreMerge())
                    .expectNext("A", "D", "B", "E", "C", "F")
                    .verifyComplete();
    }

    @Test
    void exploreMergeWith() {
        StepVerifier.create(fluxAndMonoGeneratorService.exploreMergeWith())
                    .expectNext("B", "A")
                    .verifyComplete();
    }

    @Test
    void exploreMergeSequential() {
        StepVerifier.create(fluxAndMonoGeneratorService.exploreMergeSequential())
                    .expectNext("A", "B", "C", "D", "E", "F")
                    .verifyComplete();
    }

    @Test
    void exploreZip() {
        StepVerifier.create(fluxAndMonoGeneratorService.exploreZip())
                    .expectNext("AD", "BE", "CF")
                    .verifyComplete();
    }

    @Test
    void exploreZipWith() {
        StepVerifier.create(fluxAndMonoGeneratorService.exploreZipWith())
                    .expectNext("AD", "BE", "CF")
                    .verifyComplete();
    }

    @Test
    void exploreZipWithMap() {
        StepVerifier.create(fluxAndMonoGeneratorService.exploreZipMap())
                    .expectNext("AD14", "BE25", "CF36")
                    .verifyComplete();
    }

    @Test
    void exploreZipWithMono() {
        StepVerifier.create(fluxAndMonoGeneratorService.exploreZipWithMono())
                    .expectNext("AB")
                    .verifyComplete();
    }
}