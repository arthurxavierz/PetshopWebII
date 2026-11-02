package com.petshop.petshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petshop.petshop.model.SolicitacaoAdocao;
import com.petshop.petshop.model.StatusSolicitacao;

public interface SolicitacaoAdocaoRepository extends JpaRepository<SolicitacaoAdocao, Long> {

    // O Spring monta as consultas pelo nome do método

    // Solicitações de um usuário (tela "Minhas solicitações")
    List<SolicitacaoAdocao> findByEmailUsuario(String emailUsuario);

    // Esse pet já tem solicitação com esse status? (ex.: já foi adotado = APROVADA)
    boolean existsByPetIdAndStatus(Long petId, StatusSolicitacao status);

    // Esse usuário já pediu esse pet?
    boolean existsByPetIdAndEmailUsuario(Long petId, String emailUsuario);

    // Existe alguma solicitação para esse pet? (para não deixar excluir o pet)
    boolean existsByPetId(Long petId);
}
