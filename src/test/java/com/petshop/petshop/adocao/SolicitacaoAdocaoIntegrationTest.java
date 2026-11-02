package com.petshop.petshop.adocao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.model.SolicitacaoAdocao;
import com.petshop.petshop.repository.PetRepository;
import com.petshop.petshop.repository.SolicitacaoAdocaoRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa application-test.properties (H2)
@Transactional // Limpa o banco após cada teste
public class SolicitacaoAdocaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private SolicitacaoAdocaoRepository solicitacaoRepository;

    private Pet criarPet(String nome) {
        Pet pet = new Pet();
        pet.setNome(nome);
        pet.setEspecie("Cachorro");
        pet.setRaca("Vira-lata");
        pet.setIdade(2);
        pet.setTutor("Abrigo");
        return petRepository.save(pet);
    }

    @Test
    @WithMockUser(authorities = { "Atendente" })
    void testSalvarSolicitacaoComDoisPets() throws Exception {
        Pet rex = criarPet("Rex");
        Pet mel = criarPet("Mel");

        mockMvc.perform(post("/adocao/salvar")
                .with(csrf())
                .param("nomeAdotante", "Carla Mendes")
                .param("telefoneAdotante", "(34) 99999-0000")
                .param("itens[0].pet.id", rex.getId().toString())
                .param("itens[0].taxaAdocao", "50.00")
                .param("itens[1].pet.id", mel.getId().toString())
                .param("itens[1].taxaAdocao", "30.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/adocao"));

        // Verifica no banco: uma solicitação com 2 pets e taxa total somada
        assertEquals(1, solicitacaoRepository.count());
        SolicitacaoAdocao salva = solicitacaoRepository.findAll().get(0);
        assertEquals(2, salva.getItens().size());
        assertEquals(80.0f, salva.getTaxaTotal(), 0.001);
    }

    @Test
    @WithMockUser(authorities = { "Atendente" })
    void testNaoPermitePetEmDuasSolicitacoes() throws Exception {
        Pet rex = criarPet("Rex");

        mockMvc.perform(post("/adocao/salvar")
                .with(csrf())
                .param("nomeAdotante", "Carla Mendes")
                .param("itens[0].pet.id", rex.getId().toString())
                .param("itens[0].taxaAdocao", "50.00"))
                .andExpect(status().is3xxRedirection());

        // Segunda solicitação com o mesmo pet: volta para o formulário com erro
        mockMvc.perform(post("/adocao/salvar")
                .with(csrf())
                .param("nomeAdotante", "Bruno Alves")
                .param("itens[0].pet.id", rex.getId().toString())
                .param("itens[0].taxaAdocao", "50.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("adocao/form"))
                .andExpect(model().hasErrors());

        assertEquals(1, solicitacaoRepository.count());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    void testTelasDeAdocaoAbrem() throws Exception {
        Pet rex = criarPet("Rex");

        // Formulário de nova solicitação lista os pets no select
        mockMvc.perform(get("/adocao/nova"))
                .andExpect(status().isOk())
                .andExpect(view().name("adocao/form"))
                .andExpect(content().string(containsString("Nova solicitação de adoção")))
                .andExpect(content().string(containsString("Rex (Cachorro)")));

        mockMvc.perform(post("/adocao/salvar")
                .with(csrf())
                .param("nomeAdotante", "Carla Mendes")
                .param("itens[0].pet.id", rex.getId().toString())
                .param("itens[0].taxaAdocao", "50.00"))
                .andExpect(status().is3xxRedirection());

        // Lista mostra a solicitação com o nome do pet e a taxa formatada
        mockMvc.perform(get("/adocao"))
                .andExpect(status().isOk())
                .andExpect(view().name("adocao/index"))
                .andExpect(content().string(containsString("Carla Mendes")))
                .andExpect(content().string(containsString("Rex")))
                .andExpect(content().string(containsString("R$ 50,00")));

        // Tela de edição abre com o item já preenchido
        Long id = solicitacaoRepository.findAll().get(0).getId();
        mockMvc.perform(get("/adocao/editar/" + id))
                .andExpect(status().isOk())
                .andExpect(view().name("adocao/form"))
                .andExpect(content().string(containsString("Editar solicitação #" + id)));
    }
}
