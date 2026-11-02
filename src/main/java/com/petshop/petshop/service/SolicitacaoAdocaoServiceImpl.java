package com.petshop.petshop.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petshop.petshop.model.ItemSolicitacao;
import com.petshop.petshop.model.Pet;
import com.petshop.petshop.model.SolicitacaoAdocao;
import com.petshop.petshop.repository.ItemSolicitacaoRepository;
import com.petshop.petshop.repository.PetRepository;
import com.petshop.petshop.repository.SolicitacaoAdocaoRepository;

@Service
public class SolicitacaoAdocaoServiceImpl implements SolicitacaoAdocaoService {

    private final SolicitacaoAdocaoRepository solicitacaoRepository;
    private final ItemSolicitacaoRepository itemRepository;
    private final PetRepository petRepository;

    public SolicitacaoAdocaoServiceImpl(SolicitacaoAdocaoRepository solicitacaoRepository,
            ItemSolicitacaoRepository itemRepository, PetRepository petRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.itemRepository = itemRepository;
        this.petRepository = petRepository;
    }

    @Override
    public List<SolicitacaoAdocao> getAllSolicitacoes() {
        return solicitacaoRepository.findAll();
    }

    @Override
    public Optional<SolicitacaoAdocao> getSolicitacaoById(Long id) {
        return solicitacaoRepository.findById(id);
    }

    /**
     * Regras de negócio (equivalente à checagem de estoque do pedido do professor):
     * - o nome do adotante é obrigatório;
     * - a solicitação precisa ter pelo menos um pet;
     * - o mesmo pet não pode aparecer duas vezes na solicitação;
     * - um pet não pode estar em duas solicitações de adoção ao mesmo tempo.
     * Se alguma regra falhar, lança IllegalStateException e o controller mostra a mensagem no formulário.
     */
    @Override
    @Transactional
    public void saveSolicitacao(SolicitacaoAdocao solicitacao) throws IllegalStateException {

        if (solicitacao.getNomeAdotante() == null || solicitacao.getNomeAdotante().isBlank()) {
            throw new IllegalStateException("Informe o nome do adotante.");
        }
        if (solicitacao.getItens() == null || solicitacao.getItens().isEmpty()) {
            throw new IllegalStateException("Adicione pelo menos um pet à solicitação.");
        }

        // Id da solicitação atual (-1 quando ainda é nova) para ignorar ela mesma na checagem
        Long idAtual = solicitacao.getId() == null ? -1L : solicitacao.getId();
        Set<Long> petsNaSolicitacao = new HashSet<>();

        for (ItemSolicitacao item : solicitacao.getItens()) {
            if (item.getPet() == null || item.getPet().getId() == null) {
                throw new IllegalStateException("Selecione o pet de cada item.");
            }

            Pet pet = petRepository.findById(item.getPet().getId())
                    .orElseThrow(() -> new IllegalStateException("Pet não encontrado."));

            if (!petsNaSolicitacao.add(pet.getId())) {
                throw new IllegalStateException("O pet " + pet.getNome() + " foi adicionado mais de uma vez.");
            }
            if (itemRepository.existsByPetIdAndSolicitacaoIdNot(pet.getId(), idAtual)) {
                throw new IllegalStateException("O pet " + pet.getNome() + " já está em outra solicitação de adoção.");
            }

            item.setPet(pet);
            item.setSolicitacao(solicitacao);
        }

        if (solicitacao.getDataSolicitacao() == null) {
            solicitacao.setDataSolicitacao(LocalDateTime.now());
        }
        solicitacao.calcularTotal();
        solicitacaoRepository.save(solicitacao);
    }

    @Override
    public void deleteSolicitacaoById(Long id) {
        solicitacaoRepository.deleteById(id);
    }
}
