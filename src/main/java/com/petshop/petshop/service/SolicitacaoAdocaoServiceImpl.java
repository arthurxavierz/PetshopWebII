package com.petshop.petshop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.model.SolicitacaoAdocao;
import com.petshop.petshop.model.StatusSolicitacao;
import com.petshop.petshop.repository.PetRepository;
import com.petshop.petshop.repository.SolicitacaoAdocaoRepository;

@Service
public class SolicitacaoAdocaoServiceImpl implements SolicitacaoAdocaoService {

    private final SolicitacaoAdocaoRepository solicitacaoRepository;
    private final PetRepository petRepository;

    public SolicitacaoAdocaoServiceImpl(SolicitacaoAdocaoRepository solicitacaoRepository, PetRepository petRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.petRepository = petRepository;
    }

    @Override
    public List<SolicitacaoAdocao> getAllSolicitacoes() {
        return solicitacaoRepository.findAll();
    }

    @Override
    public List<SolicitacaoAdocao> getSolicitacoesDoUsuario(String emailUsuario) {
        return solicitacaoRepository.findByEmailUsuario(emailUsuario);
    }

    @Override
    public Optional<SolicitacaoAdocao> getSolicitacaoById(Long id) {
        return solicitacaoRepository.findById(id);
    }

    @Override
    public List<Pet> getPetsDisponiveis() {
        return petRepository.findAll().stream()
                .filter(p -> !solicitacaoRepository.existsByPetIdAndStatus(p.getId(), StatusSolicitacao.APROVADA))
                .toList();
    }

    /**
     * Regras de negócio (equivalente à checagem de estoque do pedido do professor):
     * - o pet precisa existir e ainda não ter sido adotado;
     * - o mesmo usuário não pode pedir o mesmo pet duas vezes.
     * Se uma regra falhar, lança IllegalStateException e o controller mostra a mensagem no formulário.
     */
    @Override
    public void enviarSolicitacao(SolicitacaoAdocao solicitacao) throws IllegalStateException {
        if (solicitacao.getPet() == null || solicitacao.getPet().getId() == null) {
            throw new IllegalStateException("Escolha um pet.");
        }

        Pet pet = petRepository.findById(solicitacao.getPet().getId())
                .orElseThrow(() -> new IllegalStateException("Pet não encontrado."));

        if (solicitacaoRepository.existsByPetIdAndStatus(pet.getId(), StatusSolicitacao.APROVADA)) {
            throw new IllegalStateException("O pet " + pet.getNome() + " já foi adotado.");
        }
        if (solicitacaoRepository.existsByPetIdAndEmailUsuario(pet.getId(), solicitacao.getEmailUsuario())) {
            throw new IllegalStateException("Você já enviou uma solicitação para o pet " + pet.getNome() + ".");
        }

        solicitacao.setPet(pet);
        solicitacao.setDataSolicitacao(LocalDateTime.now());
        solicitacao.setStatus(StatusSolicitacao.PENDENTE);
        solicitacaoRepository.save(solicitacao);
    }

    @Override
    public void aprovarSolicitacao(Long id) throws IllegalStateException {
        SolicitacaoAdocao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Solicitação não encontrada."));

        // Um pet só pode ser adotado uma vez
        if (solicitacaoRepository.existsByPetIdAndStatus(solicitacao.getPet().getId(), StatusSolicitacao.APROVADA)) {
            throw new IllegalStateException("O pet " + solicitacao.getPet().getNome() + " já foi adotado.");
        }

        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        solicitacaoRepository.save(solicitacao);
    }
}
