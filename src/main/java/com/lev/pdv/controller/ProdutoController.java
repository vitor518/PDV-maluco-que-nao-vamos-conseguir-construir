package com.lev.pdv.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.lev.pdv.service.ProdutoService;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.lev.pdv.entity.Produto;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService service;

    @GetMapping
    public List<Produto> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
public void deletar(@PathVariable Long id) {
    service.deletar(id);
}

@PutMapping("/{id}")
public Produto atualizar(@PathVariable Long id, @RequestBody Produto produto) {
    return service.atualizar(id, produto);
}
    @PostMapping
    public Produto criaProduto(@RequestBody Produto produto) {
        return service.salvar(produto);
    }
    
    @GetMapping("/{id}")
public Produto buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id);
}

}