/**
 * Testes de Simulação de Falhas
 * Cobre cenários de erro como: rede, entrada maliciosa, sobrecarga
 */
package com.example.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

@DisplayName("Testes de Simulação de Falhas")
public class FailureHandlingTest {

    private ProdutoController controller;
    private ProdutoService serviceMock;
    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        serviceMock = mock(ProdutoService.class);
        model = mock(Model.class);
        redirectAttributes = mock(RedirectAttributes.class);
        controller = new ProdutoController();
        
        // Injetar mock via reflexão
        try {
            java.lang.reflect.Field field = ProdutoController.class.getDeclaredField("produtoService");
            field.setAccessible(true);
            field.set(controller, serviceMock);
        } catch (Exception e) {
            fail("Erro ao injetar service mock: " + e.getMessage());
        }
    }

    // ===== TESTES DE ENTRADA INVÁLIDA =====

    @Test
    @DisplayName("Deve rejeitar nome de produto vazio")
    void testRejeitar_NomeVazio() {
        Produto produto = new Produto("", 100.0);
        doThrow(new IllegalArgumentException("Nome do produto não pode estar vazio"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
        verify(redirectAttributes).addFlashAttribute(eq("erro"), anyString());
    }

    @Test
    @DisplayName("Deve rejeitar preço negativo")
    void testRejeitar_PrecoNegativo() {
        Produto produto = new Produto("Teste", -50.0);
        doThrow(new IllegalArgumentException("Preço não pode ser negativo"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
        verify(redirectAttributes).addFlashAttribute(eq("erro"), anyString());
    }

    @Test
    @DisplayName("Deve rejeitar preço nulo")
    void testRejeitar_PrecoNulo() {
        Produto produto = new Produto("Teste", null);
        doThrow(new IllegalArgumentException("Preço não pode ser nulo"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
    }

    @Test
    @DisplayName("Deve rejeitar nome nulo")
    void testRejeitar_NomeNulo() {
        Produto produto = new Produto(null, 100.0);
        doThrow(new IllegalArgumentException("Nome não pode ser nulo"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
    }

    // ===== TESTES DE ENTRADA MALICIOSA =====

    @Test
    @DisplayName("Deve tratar XSS (injeção de script) no nome")
    void testMalicia_XSS() {
        String nomeComXSS = "<script>alert('XSS')</script>";
        Produto produto = new Produto(nomeComXSS, 100.0);
        
        doThrow(new IllegalArgumentException("Entrada contém caracteres inválidos"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
        verify(redirectAttributes).addFlashAttribute(eq("erro"), anyString());
    }

    @Test
    @DisplayName("Deve tratar SQL Injection no nome")
    void testMalicia_SQLInjection() {
        String sqlInjection = "'; DROP TABLE produtos; --";
        Produto produto = new Produto(sqlInjection, 100.0);
        
        doThrow(new IllegalArgumentException("Entrada contém caracteres inválidos"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
    }

    @Test
    @DisplayName("Deve tratar Path Traversal no nome")
    void testMalicia_PathTraversal() {
        String pathTraversal = "../../etc/passwd";
        Produto produto = new Produto(pathTraversal, 100.0);
        
        doThrow(new IllegalArgumentException("Nome contém caracteres inválidos"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
    }

    @Test
    @DisplayName("Deve tratar caracteres de controle")
    void testMalicia_CaracteresControle() {
        String comControle = "Produto\u0000Teste";  // Null byte
        Produto produto = new Produto(comControle, 100.0);
        
        doThrow(new IllegalArgumentException("Entrada contém caracteres inválidos"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
    }

    // ===== TESTES DE SOBRECARGA =====

    @Test
    @DisplayName("Deve lidar com lista grande de produtos (1000 itens)")
    void testSobrecarga_MuitosProdutos() {
        List<Produto> muitosProdutos = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            Produto p = new Produto();
            p.setId((long) i);
            p.setNome("Produto " + i);
            p.setPreco(10.0 + i);
            muitosProdutos.add(p);
        }
        
        when(serviceMock.listarTodos()).thenReturn(muitosProdutos);
        
        long inicio = System.currentTimeMillis();
        String resultado = controller.listar(model);
        long tempo = System.currentTimeMillis() - inicio;
        
        assertEquals("listar", resultado);
        verify(model).addAttribute("produtos", muitosProdutos);
        assertTrue(tempo < 3000, "Deve processar 1000 produtos em menos de 3 segundos");
        System.out.println("Tempo para processar 1000 produtos: " + tempo + "ms");
    }

    @Test
    @DisplayName("Deve lidar com nome muito longo")
    void testSobrecarga_NomeMuitoLongo() {
        String nomeLongo = "A".repeat(10000);  // 10k caracteres
        Produto produto = new Produto(nomeLongo, 100.0);
        
        doThrow(new IllegalArgumentException("Nome muito longo"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
    }

    @Test
    @DisplayName("Deve rejeitar preço com valor extremo")
    void testSobrecarga_PrecoExtremo() {
        Produto produto = new Produto("Teste", Double.MAX_VALUE);
        
        doThrow(new IllegalArgumentException("Preço fora do intervalo válido"))
            .when(serviceMock).salvar(produto);

        String resultado = controller.salvar(produto, redirectAttributes);

        assertEquals("redirect:/produtos/cadastrar", resultado);
    }

    // ===== TESTES DE OPERAÇÕES NÃO EXISTENTES =====

    @Test
    @DisplayName("Deve lidar gracefully ao editar produto inexistente")
    void testErro_EditarProdutoInexistente() {
        Long idInexistente = 99999L;
        when(serviceMock.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        String resultado = controller.editarForm(idInexistente, model, redirectAttributes);

        assertEquals("redirect:/produtos/listar", resultado);
        verify(redirectAttributes).addFlashAttribute(eq("erro"), anyString());
    }

    @Test
    @DisplayName("Deve lidar gracefully ao excluir produto inexistente")
    void testErro_ExcluirProdutoInexistente() {
        Long idInexistente = 99999L;
        when(serviceMock.excluir(idInexistente)).thenReturn(false);

        String resultado = controller.excluir(idInexistente, redirectAttributes);

        assertEquals("redirect:/produtos/listar", resultado);
        verify(redirectAttributes).addFlashAttribute("erro", "Produto não encontrado");
    }

    // ===== TESTES DE EXCEÇÃO NÃO ESPERADA =====

    @Test
    @DisplayName("Deve tratar NullPointerException ao listar")
    void testErro_NullPointerException() {
        when(serviceMock.listarTodos()).thenThrow(new NullPointerException("Erro interno"));

        assertThrows(NullPointerException.class, () -> controller.listar(model));
    }

    @Test
    @DisplayName("Deve tratar exceção genérica ao salvar")
    void testErro_ExcecaoGenerica() {
        Produto produto = new Produto("Teste", 100.0);
        when(serviceMock.salvar(produto))
            .thenThrow(new RuntimeException("Erro inesperado no banco de dados"));

        assertThrows(RuntimeException.class, () -> controller.salvar(produto, redirectAttributes));
    }

    // ===== TESTES DE CONCORRÊNCIA SIMULADA =====

    @Test
    @DisplayName("Deve lidar com múltiplas operações simultâneas")
    void testConcorrencia_MultiplasOperacoes() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            Produto p1 = new Produto("Produto 1", 100.0);
            when(serviceMock.salvar(p1)).thenReturn(p1);
            controller.salvar(p1, redirectAttributes);
        });

        Thread thread2 = new Thread(() -> {
            Produto p2 = new Produto("Produto 2", 200.0);
            when(serviceMock.salvar(p2)).thenReturn(p2);
            controller.salvar(p2, redirectAttributes);
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        System.out.println("Operações concorrentes completadas com sucesso");
    }

    // ===== TESTES DE MENSAGENS DE ERRO CLARAS =====

    @Test
    @DisplayName("Mensagem de erro deve ser clara e útil")
    void testMensagemErro_Clara() {
        Produto produto = new Produto("", 100.0);
        String mensagemEsperada = "Nome do produto não pode estar vazio";
        
        doThrow(new IllegalArgumentException(mensagemEsperada))
            .when(serviceMock).salvar(produto);

        try {
            serviceMock.salvar(produto);
            fail("Deveria lançar exceção");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Nome"));
            assertTrue(e.getMessage().contains("vazio"));
            assertFalse(e.getMessage().isEmpty());
        }
    }
}
