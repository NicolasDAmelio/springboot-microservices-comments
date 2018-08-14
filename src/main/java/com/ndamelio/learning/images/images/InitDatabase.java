package com.ndamelio.learning.springbootmicroservicesimages.images;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoOperations;

//@Component
public class InitDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(InitDatabase.class);

//    @Bean
    CommandLineRunner init(MongoOperations operations) {
        return args -> {
            operations.dropCollection(Image.class);

            operations.insert(new Image("1", "learning-spring-boot-cover.jpg"));
            operations.insert(new Image("2", "learning-spring-boot-2nd-edition-cover.jpg"));
            operations.insert(new Image("3", "bazinga.png"));

            operations.findAll(Image.class).forEach(image -> LOG.info(image.toString()));
        };
    }
}
