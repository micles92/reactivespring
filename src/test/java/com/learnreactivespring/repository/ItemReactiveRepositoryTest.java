package com.learnreactivespring.repository;


import com.learnreactivespring.document.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
@DirtiesContext
public class ItemReactiveRepositoryTest {

    @Autowired
    private ItemReactiveRepository itemReactiveRepository;

    List<Item> itemList = Arrays.asList(new Item(null, "Samsung Tv", 400.0),
            new Item(null, "LG Tv", 420.0),
            new Item(null, "Apple Watch ", 299.9),
            new Item("ABC", "BOSE Headphones", 299.9),
            new Item(null, "Beats Headphones", 149.99));


    @Before
    public void setUp() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(itemList))
                .flatMap(itemReactiveRepository::save)
                .doOnNext((item -> {
                    System.out.println("Inserted item is: " + item);
                })).blockLast();
    }

    @Test
    public void getAllItems() {
        StepVerifier.create(itemReactiveRepository.findAll())
                .expectSubscription()
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void getItemById() {
        StepVerifier.create(itemReactiveRepository.findById("ABC"))
                .expectSubscription()
                .expectNextMatches((item -> item.getDescription().equals("BOSE Headphones")))
                .verifyComplete();
    }

    @Test
    public void getItemByDescription() {
        StepVerifier.create(itemReactiveRepository.findByDescription("BOSE Headphones"))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void saveItem() {
        Item item = new Item(null, "Google Home Mini", 30.00);
        Mono<Item> savedItem = itemReactiveRepository.save(item);
        StepVerifier.create(savedItem)
                .expectSubscription()
                .expectNextMatches(
                        (item1 -> item1.getId() != null && item1.getDescription().equals("Google Home Mini")))
                .verifyComplete();

    }

    @Test
    public void updateItem() {
        double newPrice = 520.0;
        Mono<Item> updatedItem = itemReactiveRepository.findByDescription("LG Tv").map((item -> {
                    item.setPrice(newPrice);
                    return item;
                })
        ).flatMap((item) -> {
            return itemReactiveRepository.save(item);
        });

        StepVerifier.create(updatedItem)
                .expectSubscription()
                .expectNextMatches(item -> item.getPrice() == newPrice)
                .verifyComplete();
    }

    @Test
    public void deleteById() {
        Mono<Void> deletedItem = itemReactiveRepository.findById("ABC")
                .map(Item::getId) //map is used to transform from one type to another type
                .flatMap((id -> {
                    return itemReactiveRepository.deleteById(id);
                }));

        StepVerifier.create(deletedItem)
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(itemReactiveRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void deleteItem() {
        Mono<Void> deletedItem = itemReactiveRepository.findByDescription("LG Tv")
                .flatMap((item -> {
                    return itemReactiveRepository.delete(item);
                }));

        StepVerifier.create(deletedItem)
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(itemReactiveRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }
}
