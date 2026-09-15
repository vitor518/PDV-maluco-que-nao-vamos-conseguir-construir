package com.lev.pdv.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numeroDaMesa;

    private LocalDateTime dataDeCriacao;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    private Double valorTotal;

    public Pedido() {
    }

    public Pedido(Integer numeroDaMesa) {
        this.numeroDaMesa = numeroDaMesa;
        this.dataDeCriacao = LocalDateTime.now();
        this.status = StatusPedido.ABERTO;
        this.valorTotal = 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroDaMesa() {
        return numeroDaMesa;
    }

    public void setNumeroDaMesa(Integer numeroDaMesa) {
        this.numeroDaMesa = numeroDaMesa;
    }

    public LocalDateTime getDataDeCriacao() {
        return dataDeCriacao;
    }

    public void setDataDeCriacao(LocalDateTime dataDeCriacao) {
        this.dataDeCriacao = dataDeCriacao;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }
}