package com.petshop.petshop.pet;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.petshop.petshop.config.TestConfig;
import com.petshop.petshop.controller.PetController;
import com.petshop.petshop.model.Pet;
import com.petshop.petshop.service.PetService;

@WebMvcTest(PetController.class)
@Import(TestConfig.class)
public class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetService petService;

    @AfterEach
    void resetMocks() {
        reset(petService);
    }

    private List<Pet> testCreatePetList() {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Luna");
        pet.setEspecie("Cachorro");
        pet.setRaca("Vira-lata");
        pet.setIdade(3);
        pet.setTutor("Maria");

        return List.of(pet);
    }

    @Test
    @DisplayName("GET /pet - Listar pets na tela index sem usuário autenticado")
    void testIndexNotAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/pet"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /pet - Listar pets na tela index com usuário logado")
    void testIndexAuthenticatedUser() throws Exception {
        when(petService.getAllPets()).thenReturn(testCreatePetList());

        mockMvc.perform(get("/pet"))
                .andExpect(status().isOk())
                .andExpect(view().name("pet/index"))
                .andExpect(model().attributeExists("pets"))
                .andExpect(content().string(containsString("Pets cadastrados")))
                .andExpect(content().string(containsString("Luna")));
    }

    @Test
    @WithMockUser(username = "admin@petshop.com", authorities = { "Admin" })
    @DisplayName("GET /pet - Exibe botão de acesso ao form de cadastro de pet para Admin")
    void testCreateFormAuthorizedUser() throws Exception {
        when(petService.getAllPets()).thenReturn(testCreatePetList());

        mockMvc.perform(get("/pet"))
                .andExpect(status().isOk())
                .andExpect(view().name("pet/index"))
                .andExpect(content().string(containsString("Cadastrar Pet")));
    }

    @Test
    @WithMockUser(username = "atendente@petshop.com", authorities = { "Atendente" })
    @DisplayName("GET /pet - Verificar que o botão de cadastrar não aparece para usuário não admin")
    void testCreateFormNotAuthorizedUser() throws Exception {
        when(petService.getAllPets()).thenReturn(testCreatePetList());

        mockMvc.perform(get("/pet"))
                .andExpect(status().isOk())
                .andExpect(view().name("pet/index"))
                .andExpect(content().string(not(containsString("Cadastrar Pet"))));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /pet/salvar - Falha na validação e retorna para o formulário")
    void testSavePetValidationError() throws Exception {
        Pet pet = new Pet(); // Pet sem nome, espécie, idade e tutor, o que causará erro de validação

        mockMvc.perform(post("/pet/salvar")
                        .with(csrf())
                        .flashAttr("pet", pet))
                .andExpect(status().isOk())
                .andExpect(view().name("pet/form"))
                .andExpect(model().attributeHasErrors("pet"));

        verify(petService, never()).savePet(any(Pet.class));
    }

    @Test
    @WithMockUser(username = "admin@petshop.com", authorities = { "Admin" })
    @DisplayName("POST /pet/salvar - Pet válido é salvo com sucesso")
    void testSaveValidPet() throws Exception {
        Pet pet = new Pet();
        pet.setNome("Thor");
        pet.setEspecie("Gato");
        pet.setRaca("Siamês");
        pet.setIdade(2);
        pet.setTutor("João");

        mockMvc.perform(post("/pet/salvar")
                        .with(csrf())
                        .flashAttr("pet", pet))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/pet"));

        verify(petService).savePet(any(Pet.class));
    }

}
