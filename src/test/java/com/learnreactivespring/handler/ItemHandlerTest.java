package com.learnreactivespring.handler;

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

import static com.learnreactivespring.constants.ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ItemHandlerTest {

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

        webTestClient.get().uri(ITEM_FUNCTIONAL_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(4);
    }

    @Test
    public void testGetAllItems_approach2() {

        webTestClient.get().uri(ITEM_FUNCTIONAL_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(4)
                .consumeWith(response -> {
                    List<Item> items = response.getResponseBody();
                    items.forEach(item1 -> {
                        assertTrue(item1.getId() != null);
                    });

                });
    }

    @Test
    public void testGetAllItems_approach3() {


        Flux<Item> itemsFlux = webTestClient.get().uri(ITEM_FUNCTIONAL_END_POINT_V1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(Item.class)
                .getResponseBody();

        StepVerifier.create(itemsFlux)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void getOneItem() {
        webTestClient.get().uri(ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1.concat("/{id}"), "ABC")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", 149.99);
    }

    @Test
    public void getOneItem_notFound() {
        webTestClient.get().uri(ItemConstants.ITEM_FUNCTIONAL_END_POINT_V1.concat("/{id}"), "DEF")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testCreateItem() {

        Item item = new Item(null, "Iphone X", 999.99);

        webTestClient.post().uri(ITEM_FUNCTIONAL_END_POINT_V1)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo("Iphone X")
                .jsonPath("$.price").isEqualTo("999.99");

    }
}
