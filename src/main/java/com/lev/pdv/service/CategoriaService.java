package com.lev.pdv.service;

import com.lev.pdv.entity.Categoria;
import com.lev.pdv.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository repository;

    public List<Categoria> listar() {
        return repository.findAll();
    }

    public Categoria salvar(Categoria categoria) {
        return repository.save(categoria);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public Categoria atualizar(Long id, Categoria categoria) {
        categoria.setId(id);
        return repository.save(categoria);
    }

    public Categoria buscarPorId(Long id) {
    return repository.findById(id).orElse(null);
}
}