package com.petshop.petshop.pet;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.repository.PetRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa application-test.properties
@Transactional // Limpa o banco após cada teste
public class PetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

    @Test
    @WithMockUser(authorities = { "Admin" })
    void testSavePetIntegration() throws Exception {

        Pet petA = new Pet();
        petA.setNome("Bob");
        petA.setEspecie("Cachorro");
        petA.setRaca("Labrador");
        petA.setIdade(5);
        petA.setTutor("Carlos");

        mockMvc.perform(post("/pet/salvar")
                .with(csrf())
                .flashAttr("pet", petA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pet"));

        // Verifica no banco se foi salvo
        assertTrue(petRepository.findAll()
                .stream()
                .anyMatch(p -> "Bob".equals(p.getNome())));
    }
}
