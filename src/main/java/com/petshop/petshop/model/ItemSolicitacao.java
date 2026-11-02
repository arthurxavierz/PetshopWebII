package com.petshop.petshop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// Equivalente ao OrderItem do professor: cada item liga um pet a uma solicitação
@Data
@Entity
@Table(name = "itens_solicitacao")
public class ItemSolicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Float taxaAdocao;

    // Vários itens podem apontar para pets diferentes; a coluna pet_id guarda qual pet
    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    // Lado "dono" da relação com a solicitação (coluna solicitacao_id)
    // Excluído do toString/equals/hashCode para não entrar em loop infinito (solicitação -> item -> solicitação...)
    @ManyToOne
    @JoinColumn(name = "solicitacao_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SolicitacaoAdocao solicitacao;
}
