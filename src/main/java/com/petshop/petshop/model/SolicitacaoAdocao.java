package com.petshop.petshop.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

// Equivalente ao pedido do professor: o usuário escolhe um pet e envia os seus dados
@Data
@Entity
@Table(name = "adocoes")
public class SolicitacaoAdocao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataSolicitacao = LocalDateTime.now();

    // Muitas solicitações podem apontar para o mesmo pet (coluna pet_id)
    @NotNull(message = "Escolha um pet")
    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @NotBlank(message = "Informe o seu nome")
    @Size(min = 3, max = 80, message = "O nome deve ter pelo menos 3 caracteres")
    private String nomeAdotante;

    @NotBlank(message = "Informe um telefone para contato")
    private String telefone;

    // Email do usuário logado que enviou (preenchido pelo controller, não pelo formulário)
    private String emailUsuario;

    // Guarda o texto do enum no banco (PENDENTE / APROVADA) em vez de um número
    @Enumerated(EnumType.STRING)
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;
}
