package com.learnreactivespring.fluxandmonoplayground;


import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FluxAndMonoTest {


    @Test
    public void fluxTest() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring")
//                .concatWith(Flux.error(new RuntimeException("Exception Occured")))
                .concatWith(Flux.just("After error"))
                .log();

        stringFlux.subscribe(System.out::println,
                (e) -> System.err.println("exception is " + e),
                () -> {
                    System.out.println("Completed");
                });
    }

    @Test
    public void fluxTestElements_WithoutError() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring")
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("Spring")
                .expectNext("Spring Boot")
                .expectNext("Reactive Spring")
                .verifyComplete();

    }

    @Test
    public void fluxTestElements_WithError() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring")
                .concatWith(Flux.error(new RuntimeException("Exception Occured")))
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("Spring")
                .expectNext("Spring Boot")
                .expectNext("Reactive Spring")
//                .expectError(RuntimeException.class)
                .expectErrorMessage("Exception Occured")
                .verify();

    }

    public void fluxTestElementsCounter_WithError() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring")
                .concatWith(Flux.error(new RuntimeException("Exception Occured")))
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(3)
                .expectErrorMessage("Exception Occured")
                .verify();

    }

    @Test
    public void fluxTestElements_WithError1() {
        Flux<String> stringFlux = Flux.just("Spring", "Spring Boot", "Reactive Spring")
                .concatWith(Flux.error(new RuntimeException("Exception Occured")))
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("Spring", "Spring Boot", "Reactive Spring")
                .expectErrorMessage("Exception Occured")
                .verify();

    }

    @Test
    public void monoTest(){
        Mono<String> stringMono = Mono.just("Spring");

        StepVerifier.create(stringMono.log())
                .expectNext("Spring")
                .verifyComplete();
    }

    @Test
    public void monoTest_withError(){

        StepVerifier.create(Mono.error(new RuntimeException("Error Occured")))
                .expectError(RuntimeException.class)
                .verify();
    }
}
