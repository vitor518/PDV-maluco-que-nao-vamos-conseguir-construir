package com.lev.pdv.service;

import com.lev.pdv.entity.Produto;
import com.lev.pdv.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository repository;

    public List<Produto> listar() {
        return repository.findAll();
    }

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public Produto atualizar(Long id, Produto produto) {
        produto.setId(id);
        return repository.save(produto);
    }

    public Produto buscarPorId(Long id) {
    return repository.findById(id).orElse(null);
}
}