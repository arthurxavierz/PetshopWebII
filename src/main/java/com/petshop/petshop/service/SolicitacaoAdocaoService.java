package com.petshop.petshop.service;

import java.util.List;
import java.util.Optional;

import com.petshop.petshop.model.SolicitacaoAdocao;

public interface SolicitacaoAdocaoService {

    List<SolicitacaoAdocao> getAllSolicitacoes();

    void saveSolicitacao(SolicitacaoAdocao solicitacao);

    Optional<SolicitacaoAdocao> getSolicitacaoById(Long id);

    void deleteSolicitacaoById(Long id);
}
