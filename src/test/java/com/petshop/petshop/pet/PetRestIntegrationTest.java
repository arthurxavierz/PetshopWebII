package com.petshop.petshop.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.model.User;
import com.petshop.petshop.repository.PetRepository;
import com.petshop.petshop.service.UserService;

import jakarta.transaction.Transactional;

/**
 * Teste de integração da API REST (/api/pets): sobe o sistema inteiro, usa o banco H2
 * e faz login com HTTP Basic (email e senha), do jeito que o Postman ou o Swagger fazem.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa application-test.properties (H2)
@Transactional // Limpa o banco após cada teste
public class PetRestIntegrationTest {

    private static final String SENHA = "teste123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserService userService;

    // Antes de cada teste, cadastra um Admin e um Usuário de verdade no banco (senha criptografada pelo service)
    @BeforeEach
    void criarUsuarios() {
        criarUsuario("Admin Teste", "admin@teste.com", "Admin");
        criarUsuario("Usuario Teste", "usuario@teste.com", "Usuario");
    }

    private void criarUsuario(String nome, String email, String perfil) {
        User user = new User();
        user.setName(nome);
        user.setEmail(email);
        user.setPassword(SENHA);
        user.setRoles(List.of(perfil));
        userService.saveUser(user);
    }

    private Pet criarPet(String nome) {
        Pet pet = new Pet();
        pet.setNome(nome);
        pet.setEspecie("Cachorro");
        pet.setRaca("Vira-lata");
        pet.setIdade(3);
        pet.setTutor("Abrigo");
        return petRepository.save(pet);
    }

    @Test
    @DisplayName("GET /api/pets sem login - 401 Unauthorized")
    void testSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/pets - Admin cadastra um pet e ele fica salvo no banco")
    void testPostCadastraNoBanco() throws Exception {
        String json = """
                {"nome": "Bidu", "especie": "Cachorro", "raca": "Beagle", "idade": 4, "tutor": "Maria"}
                """;

        mockMvc.perform(post("/api/pets")
                .with(httpBasic("admin@teste.com", SENHA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Bidu"));

        // Verifica no banco se foi salvo
        assertTrue(petRepository.findAll()
                .stream()
                .anyMatch(p -> "Bidu".equals(p.getNome()) && "Beagle".equals(p.getRaca())));
    }

    @Test
    @DisplayName("GET, PUT e DELETE /api/pets/{id} - Admin consulta, altera e exclui no banco")
    void testConsultaAlteraExclui() throws Exception {
        Pet pet = criarPet("Rex");

        // GET: devolve o pet em JSON
        mockMvc.perform(get("/api/pets/" + pet.getId()).with(httpBasic("admin@teste.com", SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rex"));

        // PUT: altera o nome e a idade
        String json = """
                {"nome": "Rex Junior", "especie": "Cachorro", "raca": "Vira-lata", "idade": 5, "tutor": "Abrigo"}
                """;
        mockMvc.perform(put("/api/pets/" + pet.getId())
                .with(httpBasic("admin@teste.com", SENHA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        Pet alterado = petRepository.findById(pet.getId()).orElseThrow();
        assertEquals("Rex Junior", alterado.getNome());
        assertEquals(5, alterado.getIdade());

        // DELETE: exclui e o pet some do banco
        mockMvc.perform(delete("/api/pets/" + pet.getId()).with(httpBasic("admin@teste.com", SENHA)))
                .andExpect(status().isNoContent());
        assertFalse(petRepository.existsById(pet.getId()));

        // GET de novo: 404
        mockMvc.perform(get("/api/pets/" + pet.getId()).with(httpBasic("admin@teste.com", SENHA)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/pets - pet inválido volta 400 e não salva")
    void testPostInvalido() throws Exception {
        long antes = petRepository.count();

        mockMvc.perform(post("/api/pets")
                .with(httpBasic("admin@teste.com", SENHA))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"\", \"especie\": \"Gato\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(antes, petRepository.count());
    }

    @Test
    @DisplayName("Usuário padrão consulta a API, mas não cadastra (403)")
    void testUsuarioNaoCadastra() throws Exception {
        long antes = petRepository.count();

        mockMvc.perform(get("/api/pets").with(httpBasic("usuario@teste.com", SENHA)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/pets")
                .with(httpBasic("usuario@teste.com", SENHA))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Tom\", \"especie\": \"Gato\", \"idade\": 1, \"tutor\": \"Ana\"}"))
                .andExpect(status().isForbidden());

        assertEquals(antes, petRepository.count());
    }
}
