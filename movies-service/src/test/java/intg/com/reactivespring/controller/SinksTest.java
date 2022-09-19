package com.reactivespring.controller;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

class SinksTest {

    @Test
    void sink() {
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = replaySink.asFlux();
        integerFlux.subscribe(integer -> System.out.println("Subscriber 1: " + integer));

        Flux<Integer> integerFlux2 = replaySink.asFlux();
        integerFlux2.subscribe(integer -> System.out.println("Subscriber 2: " + integer));

        replaySink.tryEmitNext(3);
    }

    @Test
    void sinkMulticast() {
        Sinks.Many<Integer> multicastSink = Sinks.many().multicast().onBackpressureBuffer();

        multicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = multicastSink.asFlux();
        integerFlux.subscribe(integer -> System.out.println("Subscriber 1: " + integer));

        Flux<Integer> integerFlux2 = multicastSink.asFlux();
        integerFlux2.subscribe(integer -> System.out.println("Subscriber 2: " + integer));

        multicastSink.tryEmitNext(3);
    }

    @Test
    void sinkUnicast() {
        Sinks.Many<Integer> unicastSink = Sinks.many().unicast().onBackpressureBuffer();

        unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = unicastSink.asFlux();
        integerFlux.subscribe(integer -> System.out.println("Subscriber 1: " + integer));

        // Throws exception as unicast sinks only allow for 1 subscriber
        Flux<Integer> integerFlux2 = unicastSink.asFlux();
        integerFlux2.subscribe(integer -> System.out.println("Subscriber 2: " + integer));

        unicastSink.tryEmitNext(3);
    }
}
