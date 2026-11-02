package com.petshop.petshop.service;

import java.util.List;
import java.util.Optional;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.model.SolicitacaoAdocao;

public interface SolicitacaoAdocaoService {

    List<SolicitacaoAdocao> getAllSolicitacoes();

    List<SolicitacaoAdocao> getSolicitacoesDoUsuario(String emailUsuario);

    Optional<SolicitacaoAdocao> getSolicitacaoById(Long id);

    // Pets que ainda podem ser adotados (sem solicitação aprovada)
    List<Pet> getPetsDisponiveis();

    void enviarSolicitacao(SolicitacaoAdocao solicitacao);

    void aprovarSolicitacao(Long id);
}
