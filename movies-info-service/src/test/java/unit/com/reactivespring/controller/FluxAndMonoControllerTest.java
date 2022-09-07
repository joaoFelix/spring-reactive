package com.reactivespring.controller;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
class FluxAndMonoControllerTest {

    @Autowired WebTestClient webTestClient;

    @Test
    void flux() {
        webTestClient.get()
                     .uri("/flux")
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(Integer.class)
                     .hasSize(3);
    }

    @Test
    void flux_withStepVerifier() {
        Flux<Integer> flux = webTestClient.get()
                                          .uri("/flux")
                                          .exchange()
                                          .expectStatus()
                                          .is2xxSuccessful()
                                          .returnResult(Integer.class)
                                          .getResponseBody();

        StepVerifier.create(flux)
                    .expectNext(1, 2, 3)
                    .verifyComplete();
    }

    @Test
    void flux_withConsumesWith() {
        webTestClient.get()
                     .uri("/flux")
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(Integer.class)
                     .consumeWith(listEntityExchangeResult -> {
                         List<Integer> responseBody = listEntityExchangeResult.getResponseBody();
                         assertEquals(3, Objects.requireNonNull(responseBody).size());
                     });
    }

    @Test
    void flux_withContains() {
        webTestClient.get()
                     .uri("/flux")
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(Integer.class)
                     .hasSize(3)
                     .contains(1, 2, 3);
    }

    @Test
    void mono() {
        webTestClient.get()
                     .uri("/mono")
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBody(String.class)
                     .isEqualTo("Hello World");
    }

    @Test
    void stream() {
        Flux<Long> flux = webTestClient.get()
                                       .uri("/stream")
                                       .exchange()
                                       .expectStatus()
                                       .is2xxSuccessful()
                                       .returnResult(Long.class)
                                       .getResponseBody();

        StepVerifier.create(flux)
                    .expectNext(0L, 1L, 2L, 3L)
                    .thenCancel()
                    .verify();
    }
}