package com.example;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.controller.ProdutoController;
import com.example.model.Produto;
import com.example.service.ProdutoService;

/**
 * Testes unitários para ProdutoController
 * Testa todos os endpoints do controlador e tratamento de erros
 */
class ProdutoControllerTest {

    private ProdutoController controller;
    private ProdutoService service;
    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        service = mock(ProdutoService.class);
        model = mock(Model.class);
        redirectAttributes = mock(RedirectAttributes.class);
        controller = new ProdutoController();
        
        // Injeta o service mock via reflexão
        try {
            java.lang.reflect.Field field = ProdutoController.class.getDeclaredField("produtoService");
            field.setAccessible(true);
            field.set(controller, service);
        } catch (Exception e) {
            fail("Falha ao injetar o service mock: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Deve listar todos os produtos com sucesso")
    void testListar() {
        // Preparação
        Produto p1 = new Produto();
        p1.setId(1L);
        p1.setNome("Produto 1");
        p1.setPreco(10.0);
        
        Produto p2 = new Produto();
        p2.setId(2L);
        p2.setNome("Produto 2");
        p2.setPreco(20.0);
        
        List<Produto> produtos = Arrays.asList(p1, p2);
        when(service.listarTodos()).thenReturn(produtos);

        // Ação
        String viewName = controller.listar(model);

        // Verificação
        assertEquals("listar", viewName);
        verify(model).addAttribute("produtos", produtos);
        verify(service).listarTodos();
    }

    @Test
    @DisplayName("Deve exibir o formulário de cadastro com novo produto")
    void testCadastrarForm() {
        // Ação
        String viewName = controller.cadastrarForm(model);

        // Verificação
        assertEquals("form", viewName);
        verify(model).addAttribute(eq("produto"), any(Produto.class));
    }

    @Test
    @DisplayName("Deve salvar o produto e redirecionar para a lista")
    void testSalvar() {
        // Preparação
        Produto produto = new Produto();
        produto.setNome("Novo Produto");
        produto.setPreco(15.0);
        
        when(service.salvar(produto)).thenReturn(produto);

        // Ação
        String viewName = controller.salvar(produto, redirectAttributes);

        // Verificação
        assertEquals("redirect:/produtos/listar", viewName);
        verify(service).salvar(produto);
        verify(redirectAttributes).addFlashAttribute("sucesso", "Produto salvo com sucesso!");
    }

    @Test
    @DisplayName("Deve tratar erro ao salvar e exibir mensagem de erro")
    void testSalvarComErro() {
        // Preparação
        Produto produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setPreco(10.0);
        
        doThrow(new IllegalArgumentException("Nome não pode ser vazio"))
            .when(service).salvar(produto);

        // Ação
        String viewName = controller.salvar(produto, redirectAttributes);

        // Verificação
        assertEquals("redirect:/produtos/cadastrar", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("erro"), anyString());
    }

    @Test
    @DisplayName("Deve exibir o formulário de edição com produto existente")
    void testEditarForm() {
        // Preparação
        Long id = 1L;
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Produto Existente");
        produto.setPreco(25.0);
        
        when(service.buscarPorId(id)).thenReturn(Optional.of(produto));

        // Ação
        String viewName = controller.editarForm(id, model, redirectAttributes);

        // Verificação
        assertEquals("form", viewName);
        verify(model).addAttribute("produto", produto);
        verify(service).buscarPorId(id);
    }

    @Test
    @DisplayName("Deve tratar formulário de edição para produto inexistente")
    void testEditarFormProdutoNaoExistente() {
        // Preparação
        Long id = 999L;
        when(service.buscarPorId(id)).thenReturn(Optional.empty());

        // Ação
        String viewName = controller.editarForm(id, model, redirectAttributes);

        // Verificação
        assertEquals("redirect:/produtos/listar", viewName);
        verify(redirectAttributes).addFlashAttribute("erro", "Produto não encontrado");
    }

    @Test
    @DisplayName("Deve excluir o produto e redirecionar para a lista")
    void testExcluir() {
        // Preparação
        Long id = 1L;
        when(service.excluir(id)).thenReturn(true);

        // Ação
        String viewName = controller.excluir(id, redirectAttributes);

        // Verificação
        assertEquals("redirect:/produtos/listar", viewName);
        verify(service).excluir(id);
        verify(redirectAttributes).addFlashAttribute("sucesso", "Produto deletado com sucesso!");
    }

    @Test
    @DisplayName("Deve tratar erro de exclusão de forma adequada")
    void testExcluirComErro() {
        // Preparação
        Long id = 999L;
        when(service.excluir(id)).thenReturn(false);

        // Ação
        String viewName = controller.excluir(id, redirectAttributes);

        // Verificação
        assertEquals("redirect:/produtos/listar", viewName);
        verify(redirectAttributes).addFlashAttribute("erro", "Produto não encontrado");
    }

    @Test
    @DisplayName("Deve tratar vários produtos na listagem")
    void testListarVariosProdutos() {
        // Preparação
        List<Produto> produtos = Arrays.asList(
            createProduto(1L, "P1", 10.0),
            createProduto(2L, "P2", 20.0),
            createProduto(3L, "P3", 30.0)
        );
        when(service.listarTodos()).thenReturn(produtos);

        // Ação
        String viewName = controller.listar(model);

        // Verificação
        assertEquals("listar", viewName);
        verify(model).addAttribute("produtos", produtos);
    }

    @Test
    @DisplayName("Deve tratar lista de produtos vazia")
    void testListarVazio() {
        // Preparação
        when(service.listarTodos()).thenReturn(Arrays.asList());

        // Ação
        String viewName = controller.listar(model);

        // Verificação
        assertEquals("listar", viewName);
        verify(model).addAttribute(eq("produtos"), anyList());
    }

    private Produto createProduto(Long id, String nome, Double preco) {
        Produto p = new Produto();
        p.setId(id);
        p.setNome(nome);
        p.setPreco(preco);
        return p;
    }
}
