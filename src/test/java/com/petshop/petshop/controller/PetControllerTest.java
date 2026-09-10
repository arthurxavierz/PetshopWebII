package com.petshop.petshop.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.service.PetService;

@WebMvcTest(PetController.class)
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @Test
    void deveListarPets() throws Exception {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Luna");
        pet.setEspecie("Cachorro");
        pet.setRaca("Vira-lata");
        pet.setIdade(3);
        pet.setTutor("Maria");

        when(petService.getAllPets()).thenReturn(List.of(pet));

        mockMvc.perform(get("/pet"))
                .andExpect(status().isOk())
                .andExpect(view().name("pet/index"))
                .andExpect(model().attributeExists("pets"));
    }

    @Test
    void deveSalvarNovoPet() throws Exception {
        mockMvc.perform(post("/pet/salvar")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nome", "Luna")
                .param("especie", "Cachorro")
                .param("raca", "Vira-lata")
                .param("idade", "3")
                .param("tutor", "Maria"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pet"));
    }
}
