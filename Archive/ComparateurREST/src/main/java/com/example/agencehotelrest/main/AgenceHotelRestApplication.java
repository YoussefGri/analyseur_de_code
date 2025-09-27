package com.example.agencehotelrest.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EntityScan(basePackages = {
        "com.example.agencehotelrest.models"
})
@EnableJpaRepositories(basePackages = {
        "com.example.agencehotelrest.repositories"
})
@SpringBootApplication(scanBasePackages = {
        "com.example.agencehotelrest",
        "com.example.agencehotelrest.data",
        "com.example.agencehotelrest.exceptions",
        "com.example.agencehotelrest.controllers"
})
public class AgenceHotelRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenceHotelRestApplication.class, args);
    }

}
