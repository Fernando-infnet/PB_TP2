package com.example;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.model.Produto;
import com.example.service.ProdutoService;

/**
 * Testes unitários para ProdutoService.
 * Testa a lógica de negócio para operações CRUD.
 */
public class AppTest {

    private ProdutoService service;

    @BeforeEach
    public void setUp() {
        service = new ProdutoService();
    }

    @Test
    public void testListarTodos() {
        List<Produto> produtos = service.listarTodos();
        assertNotNull(produtos, "Lista de produtos não deveria ser nula");
        assertTrue(produtos.size() > 0, "Deveria conter produtos iniciais");
    }

    @Test
    public void testBuscarPorIdExistente() {
        Optional<Produto> produto = service.buscarPorId(1L);
        assertTrue(produto.isPresent(), "Produto com ID 1 deveria existir");
        assertEquals("Notebook", produto.get().getNome());
        assertEquals(3000.0, produto.get().getPreco());
    }

    @Test
    public void testBuscarPorIdNaoExistente() {
        Optional<Produto> produto = service.buscarPorId(99999L);
        assertFalse(produto.isPresent(), "Produto com ID 99999 não deveria existir");
    }

    @Test
    public void testSalvarNovoProduto() {
        Produto novoProduto = new Produto("Novo Produto", 150.0);
        Produto salvo = service.salvar(novoProduto);
        
        assertNotNull(salvo.getId(), "Produto salvo deveria ter um ID");
        assertEquals("Novo Produto", salvo.getNome());
        assertEquals(150.0, salvo.getPreco());
        
        // Verifica se está na lista
        List<Produto> produtos = service.listarTodos();
        assertTrue(produtos.stream().anyMatch(p -> p.getId().equals(salvo.getId())));
    }

    @Test
    public void testAtualizarProduto() {
        Produto produtoOriginal = service.buscarPorId(1L).get();
        String nomeOriginal = produtoOriginal.getNome();
        
        produtoOriginal.setNome("Notebook Atualizado");
        service.salvar(produtoOriginal);
        
        Produto atualizado = service.buscarPorId(1L).get();
        assertEquals("Notebook Atualizado", atualizado.getNome());
        assertNotEquals(nomeOriginal, atualizado.getNome());
    }

    @Test
    public void testExcluirProduto() {
        // Cria um produto para excluir
        Produto novo = new Produto("Produto para Deletar", 50.0);
        Produto salvo = service.salvar(novo);
        Long idParaDeletar = salvo.getId();
        
        // Verifica se existe
        assertTrue(service.buscarPorId(idParaDeletar).isPresent());
        
        // Exclui
        boolean resultado = service.excluir(idParaDeletar);
        assertTrue(resultado, "Exclusão deveria retornar true");
        
        // Verifica se não existe mais
        assertFalse(service.buscarPorId(idParaDeletar).isPresent());
    }

    @Test
    public void testExcluirProdutoNaoExistente() {
        boolean resultado = service.excluir(99999L);
        assertFalse(resultado, "Exclusão de produto inexistente deveria retornar false");
    }

    @Test
    public void testSalvarComNomeVazio() {
        Produto produto = new Produto("", 50.0);
        assertThrows(IllegalArgumentException.class, () -> service.salvar(produto),
            "Deveria lançar exceção para nome vazio");
    }

    @Test
    public void testSalvarComNomeNulo() {
        Produto produto = new Produto(null, 50.0);
        assertThrows(IllegalArgumentException.class, () -> service.salvar(produto),
            "Deveria lançar exceção para nome nulo");
    }

    @Test
    public void testSalvarComPrecoNegativo() {
        Produto produto = new Produto("Produto", -10.0);
        assertThrows(IllegalArgumentException.class, () -> service.salvar(produto),
            "Deveria lançar exceção para preço negativo");
    }

    @Test
    public void testSalvarComPrecoNulo() {
        Produto produto = new Produto("Produto", null);
        assertThrows(IllegalArgumentException.class, () -> service.salvar(produto),
            "Deveria lançar exceção para preço nulo");
    }

    @Test
    public void testValidarProdutoValido() {
        Produto produto = new Produto("Produto Válido", 100.0);
        Produto salvo = service.salvar(produto);
        assertNotNull(salvo.getId(), "Produto válido deveria ser salvo com sucesso");
    }
}

