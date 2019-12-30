package com.learnreactivespring;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static reactor.core.scheduler.Schedulers.parallel;

public class FluxAndMonoTransformTest {

    List<String> names = Arrays.asList("adam", "anna", "jack", "jenny");


    @Test
    public void transformUsingMap() {
        Flux<String> namesFlux = Flux.fromIterable(names)
                .map(s -> s.toUpperCase())
                .log();

        StepVerifier.create(namesFlux)
                .expectNext("ADAM", "ANNA", "JACK", "JENNY")
                .verifyComplete();
    }

    @Test
    public void transformUsingMap_Length() {
        Flux<Integer> namesFlux = Flux.fromIterable(names)
                .map(s -> s.length())
                .log();

        StepVerifier.create(namesFlux)
                .expectNext(4, 4, 4, 5)
                .verifyComplete();
    }

    @Test
    public void transformUsingMap_Length_repeat() {
        Flux<Integer> namesFlux = Flux.fromIterable(names)
                .map(s -> s.length())
                .repeat(1)
                .log();

        StepVerifier.create(namesFlux)
                .expectNext(4, 4, 4, 5)
                .expectNext(4, 4, 4, 5)
                .verifyComplete();
    }

    @Test
    public void transformUsingMap_Filter() {
        Flux<String> namesFlux = Flux.fromIterable(names)
                .filter(s -> s.length() > 4)
                .map(s -> s.toUpperCase())
                .log();

        StepVerifier.create(namesFlux)
                .expectNext("JENNY")
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMap() {
        Flux<String> namesFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F")) //A,B,C,D,E,F
                .flatMap(s -> {
                    return Flux.fromIterable(convertToList(s)); // A -> List[A, newValue], B-> List[B, newValue]
                })  //db or external service call that returns a flux -> s -> Flux<String>
                .log();

        StepVerifier.create(namesFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMap_usingParallel() {
        Flux<String> namesFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F")) // Flux<String>
                .window(2) // Flux<Flux<String> -> (A,B), (C,D), (E,F) | window() uzywane jest do przekazywania wiecej elementow z Flux
                .flatMap((s) ->
                        s.map(this::convertToList).subscribeOn(parallel())) // Flux<List<String>  | parallel -> czuwa by rzeczy wykonywaly sie rownolegle
                .flatMap(s -> Flux.fromIterable(s))   // Flux<String>
                .log();

        StepVerifier.create(namesFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMap_parallel_maintain_order() {
        Flux<String> namesFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F")) // Flux<String>
                .window(2) // Flux<Flux<String> -> (A,B), (C,D), (E,F) | window() uzywane jest do przekazywania wiecej elementow z Flux
//                .concatMap((s) ->
//                        s.map(this::convertToList).subscribeOn(parallel())) // Flux<List<String>  | parallel -> czuwa by rzeczy wykonywaly sie rownolegle

                .flatMapSequential((s) ->
                        s.map(this::convertToList).subscribeOn(parallel()))
                .flatMap(s -> Flux.fromIterable(s))   // Flux<String>
                .log();

        StepVerifier.create(namesFlux)
                .expectNextCount(12)
                .verifyComplete();
    }


    private List<String> convertToList(String s) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList(s, "newValue");
    }
}
