package com.petshop.petshop.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.petshop.petshop.service.PetService;

@TestConfiguration
public class TestConfig {

    @Bean
    public PetService petService() {
        return Mockito.mock(PetService.class);
    }

}
