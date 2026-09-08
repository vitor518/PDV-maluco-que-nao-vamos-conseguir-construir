package com.lev.pdv.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Produto {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
private String nome;
private Double preco;
private Integer quantidade;
private String descricao;

public Produto(String nome, Double preco, Integer quantidade, String descricao) {
    this.nome = nome;
    this.preco = preco;
    this.quantidade = quantidade;
    this.descricao = descricao;

}
public Long getId() {
    return id;
}
public String getNome() {
    return nome;
}
public Double getPreco() {
    return preco;
}
public Integer getQuantidade() {
    return quantidade;
}
public String getDescricao() {
    return descricao;
}

public void setId(Long id) {
    this.id = id;
}
public void setNome(String nome) {
    this.nome = nome;
}
public void setPreco(Double preco) {
    this.preco = preco;
    }
public void setQuantidade(Integer quantidade) {
    this.quantidade = quantidade;
    }
public void setDescricao(String descricao) {
    this.descricao = descricao;
    }
    public Produto() {
    }
}
