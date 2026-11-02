package com.petshop.petshop.adocao;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.model.SolicitacaoAdocao;
import com.petshop.petshop.model.StatusSolicitacao;
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

    private void enviarSolicitacao(String email, Pet pet) throws Exception {
        mockMvc.perform(post("/adocao/salvar")
                .with(user(email).authorities(new SimpleGrantedAuthority("Usuario")))
                .with(csrf())
                .param("pet.id", pet.getId().toString())
                .param("nomeAdotante", "Carla Mendes")
                .param("telefone", "(34) 99999-0000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/adocao"));
    }

    @Test
    void testUsuarioEnviaEAdminAprova() throws Exception {
        Pet rex = criarPet("Rex");

        // Usuário padrão envia a solicitação: fica PENDENTE e guarda o email de quem enviou
        enviarSolicitacao("usuario@petshop.com", rex);
        SolicitacaoAdocao salva = solicitacaoRepository.findAll().get(0);
        assertEquals(StatusSolicitacao.PENDENTE, salva.getStatus());
        assertEquals("usuario@petshop.com", salva.getEmailUsuario());

        // Admin aprova
        mockMvc.perform(get("/adocao/aprovar/" + salva.getId())
                .with(user("admin@petshop.com").authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().is3xxRedirection());
        assertEquals(StatusSolicitacao.APROVADA, solicitacaoRepository.findById(salva.getId()).get().getStatus());
    }

    @Test
    void testUsuarioPadraoNaoAprova() throws Exception {
        Pet rex = criarPet("Rex");
        enviarSolicitacao("usuario@petshop.com", rex);
        Long id = solicitacaoRepository.findAll().get(0).getId();
        mockMvc.perform(get("/adocao/aprovar/" + id).with(user("usuario@petshop.com").authorities(new SimpleGrantedAuthority("Usuario"))))
                .andExpect(status().isForbidden());

        assertEquals(StatusSolicitacao.PENDENTE, solicitacaoRepository.findById(id).get().getStatus());
    }

    @Test
    void testNaoSolicitaPetJaAdotado() throws Exception {
        Pet rex = criarPet("Rex");
        enviarSolicitacao("usuario@petshop.com", rex);
        Long id = solicitacaoRepository.findAll().get(0).getId();
        mockMvc.perform(get("/adocao/aprovar/" + id).with(user("admin@petshop.com").authorities(new SimpleGrantedAuthority("Admin"))));

        // Outro usuário tenta pedir o mesmo pet: volta para o formulário com erro
        mockMvc.perform(post("/adocao/salvar")
                .with(user("outro@petshop.com").authorities(new SimpleGrantedAuthority("Usuario")))
                .with(csrf())
                .param("pet.id", rex.getId().toString())
                .param("nomeAdotante", "Bruno Alves")
                .param("telefone", "(34) 98888-0000"))
                .andExpect(status().isOk())
                .andExpect(view().name("adocao/form"))
                .andExpect(model().hasErrors());

        assertEquals(1, solicitacaoRepository.count());
    }

    @Test
    void testTelasDeAdocaoAbrem() throws Exception {
        Pet rex = criarPet("Rex");

        // Formulário do usuário padrão lista o pet disponível
        mockMvc.perform(get("/adocao/nova").with(user("usuario@petshop.com").authorities(new SimpleGrantedAuthority("Usuario"))))
                .andExpect(status().isOk())
                .andExpect(view().name("adocao/form"))
                .andExpect(content().string(containsString("Solicitar adoção")))
                .andExpect(content().string(containsString("Rex (Cachorro")));

        enviarSolicitacao("usuario@petshop.com", rex);

        // Admin vê a solicitação com o botão Aprovar
        mockMvc.perform(get("/adocao").with(user("admin@petshop.com").authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Carla Mendes")))
                .andExpect(content().string(containsString("Aprovar")));

        // Usuário padrão vê "Minhas solicitações"
        mockMvc.perform(get("/adocao").with(user("usuario@petshop.com").authorities(new SimpleGrantedAuthority("Usuario"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Minhas solicitações")))
                .andExpect(content().string(containsString("Pendente")));
    }
}
