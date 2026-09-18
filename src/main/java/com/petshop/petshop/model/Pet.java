package com.petshop.petshop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é um campo obrigatório")
    @Size(min = 3, max = 50, message = "Nome deve conter pelo menos 3 caracteres")
    private String nome;

    @NotBlank(message = "Espécie é um campo obrigatório")
    private String especie;

    private String raca;

    @NotNull(message = "Informe uma idade válida")
    @Min(value = 0, message = "A idade não pode ser negativa")
    private Integer idade;

    @NotBlank(message = "Tutor é um campo obrigatório")
    @Size(min = 3, max = 80, message = "Nome do tutor deve conter pelo menos 3 caracteres")
    private String tutor;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEspecie() {
        return especie;
    }

    public String getRaca() {
        return raca;
    }

    public Integer getIdade() {
        return idade;
    }

    public String getTutor() {
        return tutor;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public void setTutor(String tutor) {
        this.tutor = tutor;
    }
}
