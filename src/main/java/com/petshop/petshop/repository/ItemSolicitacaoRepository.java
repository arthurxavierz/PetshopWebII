package com.petshop.petshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petshop.petshop.model.ItemSolicitacao;

public interface ItemSolicitacaoRepository extends JpaRepository<ItemSolicitacao, Long> {

    // O Spring monta a consulta pelo nome do método:
    // existe algum item com esse pet?
    boolean existsByPetId(Long petId);

    // existe algum item com esse pet em uma solicitação DIFERENTE desta?
    boolean existsByPetIdAndSolicitacaoIdNot(Long petId, Long solicitacaoId);
}
