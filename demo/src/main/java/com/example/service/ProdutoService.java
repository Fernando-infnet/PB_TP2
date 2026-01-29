package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.model.Produto;

/**
 * Camada de serviço para operações de produtos.
 * Fornece funcionalidades CRUD e lógica de negócio para gerenciar produtos.
 */
@Service
public class ProdutoService {

    private final List<Produto> produtos = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Inicializa com dados de exemplo
     */
    public ProdutoService() {
        produtos.add(new Produto(idGenerator.getAndIncrement(), "Notebook", 3000.0));
        produtos.add(new Produto(idGenerator.getAndIncrement(), "Mouse", 50.0));
        produtos.add(new Produto(idGenerator.getAndIncrement(), "Teclado", 150.0));
    }

    /**
     * Recupera todos os produtos
     */
    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos);
    }

    /**
     * Encontra produto por ID
     */
    public Optional<Produto> buscarPorId(Long id) {
        return produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    /**
     * Salva ou atualiza um produto
     */
    public Produto salvar(Produto produto) {
        validarProduto(produto);

        if (produto.getId() == null) {
            produto.setId(idGenerator.getAndIncrement());
            produtos.add(produto);
        } else {
            produtos.stream()
                    .filter(p -> p.getId().equals(produto.getId()))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setNome(produto.getNome());
                        p.setPreco(produto.getPreco());
                    });
        }
        return produto;
    }

    /**
     * Deleta produto por ID
     */
    public boolean excluir(Long id) {
        return produtos.removeIf(p -> p.getId().equals(id));
    }

    /**
     * Valida dados do produto
     */
    private void validarProduto(Produto produto) {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode estar vazio");
        }
        if (produto.getPreco() == null || produto.getPreco() < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo ou nulo");
        }
    }
}
