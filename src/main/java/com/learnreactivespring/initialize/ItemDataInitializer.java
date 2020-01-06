package com.learnreactivespring.initialize;

import com.learnreactivespring.document.Item;
import com.learnreactivespring.repository.ItemReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
public class ItemDataInitializer implements CommandLineRunner {

    @Autowired
    private ItemReactiveRepository itemReactiveRepository;

    @Override
    public void run(String... args) throws Exception {
        initialDataSetup();
    }

    private void initialDataSetup() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(data())).flatMap(itemReactiveRepository::save)
                .thenMany(itemReactiveRepository.findAll()).subscribe(item -> System.out.println("Inserted from initializer:" + item));
    }

    public List<Item> data() {
        return Arrays.asList(new Item(null, "Samsung Tv", 399.9),
                new Item(null, "LG Tv", 329.9),
                new Item(null, "Apple Watch", 349.9),
                new Item("ABC", "Beats HeadPhones", 19.99));
    }
}
