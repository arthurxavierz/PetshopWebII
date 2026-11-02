package com.petshop.petshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Data;

// Equivalente ao Order (pedido) do professor: uma solicitação tem vários itens (pets)
@Data
@Entity
@Table(name = "solicitacoes_adocao")
public class SolicitacaoAdocao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataSolicitacao = LocalDateTime.now();

    private String nomeAdotante;

    private String telefoneAdotante;

    // mappedBy: quem guarda a chave estrangeira é o ItemSolicitacao (coluna solicitacao_id)
    // cascade ALL: salvar/excluir a solicitação salva/exclui os itens junto
    // orphanRemoval: item removido da lista é apagado do banco
    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemSolicitacao> itens = new ArrayList<>();

    private Float taxaTotal = 0.0f;

    public void addItem(ItemSolicitacao item) {
        itens.add(item);
        item.setSolicitacao(this);
    }

    // Soma a taxa de adoção de cada pet (equivalente ao calculateTotal do pedido)
    public void calcularTotal() {
        this.taxaTotal = (float) itens.stream()
                .mapToDouble(i -> i.getTaxaAdocao() == null ? 0 : i.getTaxaAdocao())
                .sum();
    }
}
