package com.lev.pdv.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.lev.pdv.service.CategoriaService;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.lev.pdv.entity.Categoria;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService service;

    @GetMapping
    public List<Categoria> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
public void deletar(@PathVariable Long id) {
    service.deletar(id);
}

@PutMapping("/{id}")
public Categoria atualizar(@PathVariable Long id, @RequestBody Categoria categoria  ) {
    return service.atualizar(id, categoria );
}
    @PostMapping
    public Categoria criaCategoria(@RequestBody Categoria categoria) {
        return service.salvar(categoria);
    }
    
    @GetMapping("/{id}")
public Categoria buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id);
}

}