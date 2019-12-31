package com.learnreactivespring.fluxandmonoplayground;

import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class ColdAndHotPublisherTest {

    @Test
    public void coldPublisherTest() throws InterruptedException {
        Flux<String> stringFlux = Flux.just("A", "B", "C", "D", "E", "F")
                .delayElements(Duration.ofSeconds(1));

        stringFlux.subscribe(s -> System.out.println("Subscriber 1: " + s)); //emits the value from beginning
        Thread.sleep(2000);

        stringFlux.subscribe(s -> System.out.println("Subscriber 2: " + s)); //emits the value from beginning

        Thread.sleep(4000);
    }

    @Test
    public void hoyPublisherTest() throws InterruptedException {
        Flux<String> stringFlux = Flux.just("A", "B", "C", "D", "E", "F")  //emits the value from beginning
                .delayElements(Duration.ofSeconds(1));


        ConnectableFlux<String> connectableFlux =  stringFlux.publish();

        connectableFlux.connect();
        connectableFlux.subscribe(s -> System.out.println("Subscriber 1: " + s));
        Thread.sleep(3000);


        connectableFlux.subscribe(s -> System.out.println("Subscriber 2: " + s)); //does not emits the value from beginning
        Thread.sleep(4000);

    }
}
