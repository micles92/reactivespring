package com.learnreactivespring.controller.v1;

import com.learnreactivespring.constants.ItemConstants;
import com.learnreactivespring.document.Item;
import com.learnreactivespring.repository.ItemReactiveRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ItemControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    private ItemReactiveRepository itemReactiveRepository;


    @Before
    public void setUp() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(data())).flatMap(itemReactiveRepository::save)
                .doOnNext((item -> System.out.println("Inserted item: is:" + item)))
                .blockLast();
    }


    public List<Item> data() {
        return Arrays.asList(new Item(null, "Samsung Tv", 399.9),
                new Item(null, "LG Tv", 329.9),
                new Item(null, "Apple Watch", 349.9),
                new Item("ABC", "Beats HeadPhones", 19.99));
    }


    @Test
    public void getAllItems() {

        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Item.class)
                .hasSize(4);
    }

    @Test
    public void getAllItems_approach2() {

        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Item.class)
                .hasSize(4)
                .consumeWith((response) -> {
                    List<Item> items = response.getResponseBody();
                    items.forEach(item -> {
                        assertNotNull(item.getId());
                    });
                });
    }

    @Test
    public void getAllItems_approach3() {

        Flux<Item> itemFlux = webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .returnResult(Item.class)
                .getResponseBody();

        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void getOneItem() {
        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "ABC")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", 149.99);
    }

    @Test
    public void getOneItem_notFound() {
        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "DEF")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void createItem() {
        Item item = new Item(null, "Iphone X", 999.99);

        webTestClient.post().uri(ItemConstants.ITEM_END_POINT_V1)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo("Iphone X")
                .jsonPath("$.price").isEqualTo(999.99);
    }

    @Test
    public void deleteItem() {
        webTestClient.delete().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "ABC")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
    }

    @Test
    public void updateItem() {
        double newPrice = 129.99;
        Item item = new Item(null, "Beats HeadPhones", newPrice);

        webTestClient.put().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "ABC")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", newPrice);
    }

    @Test
    public void updateItem_notFound() {
        double newPrice = 129.99;
        Item item = new Item(null, "Beats HeadPhones", newPrice);

        webTestClient.put().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "DEF")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isNotFound();

    }
}
