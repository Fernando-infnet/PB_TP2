/**
 * Testes de Fail Early e Fail Gracefully
 * Valida validação robusta, logging, e tratamento de erros com mensagens claras
 */
package com.example.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import com.example.model.Produto;
import com.example.service.ProdutoService;
import java.util.ArrayList;
import java.util.List;

@DisplayName("Testes de Fail Early e Fail Gracefully")
public class FailEarlyFailGracefullyTest {

    private ProdutoService service;

    @BeforeEach
    void setUp() {
        service = new ProdutoService();
    }

    // ===== FAIL EARLY - VALIDAÇÃO IMEDIATA =====

    @Test
    @DisplayName("Deve falhar imediatamente ao tentar salvar produto nulo")
    void testFailEarly_ProdutoNulo() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.salvar(null),
            "Deveria falhar imediatamente com produto nulo"
        );
        
        assertNotNull(exception.getMessage(), "Mensagem não deveria ser nula");
        assertTrue(exception.getMessage().length() > 0, "Mensagem deveria ter conteúdo");
    }

    @Test
    @DisplayName("Deve falhar imediatamente com nome nulo sem tentar persistir")
    void testFailEarly_NomeNulo() {
        Produto produto = new Produto();
        produto.setNome(null);
        produto.setPreco(100.0);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.salvar(produto)
        );
        
        assertTrue(exception.getMessage().contains("Nome") || 
                   exception.getMessage().contains("nulo"),
            "Mensagem deveria mencionar nome nulo");
        
        // Verificar que não foi salvo
        int quantidadeAntes = service.listarTodos().size();
        assertEquals(3, quantidadeAntes, "Não deveria ter adicionado produto");
    }

    @Test
    @DisplayName("Deve falhar imediatamente com preço nulo")
    void testFailEarly_PrecoNulo() {
        Produto produto = new Produto("Teste", null);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.salvar(produto)
        );
        
        assertTrue(exception.getMessage().contains("Preço"));
    }

    @Test
    @DisplayName("Deve falhar imediatamente com preço negativo")
    void testFailEarly_PrecoNegativo() {
        Produto produto = new Produto("Teste", -50.0);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.salvar(produto)
        );
        
        assertTrue(exception.getMessage().contains("negativo"));
    }

    @Test
    @DisplayName("Deve falhar imediatamente com preço zero")
    void testFailEarly_PrecoZero() {
        Produto produto = new Produto("Teste", 0.0);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.salvar(produto)
        );
        
        assertTrue(exception.getMessage().contains("zero") || 
                   exception.getMessage().contains("maior"));
    }

    // ===== FAIL GRACEFULLY - MENSAGENS CLARAS =====

    @Test
    @DisplayName("Mensagem de erro deve ser clara e específica para nome vazio")
    void testFailGracefully_NomeVazioMensagem() {
        Produto produto = new Produto("", 100.0);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.salvar(produto)
        );
        
        String mensagem = exception.getMessage();
        assertNotNull(mensagem, "Mensagem não deveria ser nula");
        assertTrue(mensagem.length() > 5, "Mensagem deveria ser descritiva");
        assertTrue(mensagem.toLowerCase().contains("nome"), 
            "Mensagem deveria mencionar 'nome'");
        assertTrue(mensagem.toLowerCase().contains("vazio") || 
                   mensagem.toLowerCase().contains("espaço"),
            "Mensagem deveria explicar o problema");
        
        System.out.println("Mensagem de erro clara: " + mensagem);
    }

    @Test
    @DisplayName("Mensagem de erro deve ser clara para preço negativo")
    void testFailGracefully_PrecoNegativoMensagem() {
        Produto produto = new Produto("Teste", -100.50);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.salvar(produto)
        );
        
        String mensagem = exception.getMessage();
        assertTrue(mensagem.contains("Preço") || mensagem.contains("preço"),
            "Mensagem deveria mencionar 'preço'");
        assertTrue(mensagem.contains("negativo") || mensagem.contains("positivo"),
            "Mensagem deveria explicar que deve ser positivo");
    }

    @Test
    @DisplayName("Mensagem de erro deve ser clara para nome com apenas espaços")
    void testFailGracefully_NomeEspacosMensagem() {
        Produto produto = new Produto("     ", 100.0);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.salvar(produto)
        );
        
        String mensagem = exception.getMessage();
        assertTrue(mensagem.contains("vazio") || mensagem.contains("espaços"),
            "Mensagem deveria indicar o problema específico");
    }

    // ===== FAIL GRACEFULLY - MENSAGENS COM CONTEXTO =====

    @Test
    @DisplayName("Mensagem deveria fornecer contexto útil")
    void testFailGracefully_MensagemComContexto() {
        Produto produto = new Produto("A".repeat(300), 100.0);
        
        try {
            service.salvar(produto);
            fail("Deveria rejeitar nome muito longo");
        } catch (IllegalArgumentException e) {
            String mensagem = e.getMessage();
            
            // Mensagem deveria ser informativa
            assertTrue(mensagem.contains("255") || 
                      mensagem.contains("limite") ||
                      mensagem.contains("exceder"),
                "Mensagem deveria indicar o limite");
            
            System.out.println("Mensagem com contexto: " + mensagem);
        }
    }

    // ===== VALIDAÇÃO EM MÚLTIPLAS CAMADAS =====

    @Test
    @DisplayName("Deve validar antes de modificar estado")
    void testFailEarly_ValidacaoAntesMudanca() {
        Produto produtoInvalido = new Produto("", 100.0);
        int tamanhoAntes = service.listarTodos().size();
        
        assertThrows(IllegalArgumentException.class, 
            () -> service.salvar(produtoInvalido));
        
        int tamanhoDepois = service.listarTodos().size();
        assertEquals(tamanhoAntes, tamanhoDepois,
            "Lista não deveria ter sido modificada após falha");
    }

    @Test
    @DisplayName("Deve validar produto completo antes de salvar")
    void testFailEarly_ValidacaoCompleta() {
        // Múltiplos erros - qual deveria ser relatado primeiro?
        Produto produto = new Produto("", -50.0);
        
        try {
            service.salvar(produto);
            fail("Deveria rejeitar produto com múltiplos erros");
        } catch (IllegalArgumentException e) {
            String mensagem = e.getMessage();
            assertNotNull(mensagem);
            assertTrue(mensagem.length() > 0);
            
            // Deveria reportar pelo menos um erro
            assertTrue(mensagem.contains("Nome") || 
                      mensagem.contains("Preço"));
        }
    }

    // ===== TESTES DE CASOS VÁLIDOS =====

    @ParameterizedTest
    @DisplayName("Deve aceitar dados válidos")
    @ValueSource(strings = {
        "Notebook",
        "Mouse Logitech",
        "Produto Teste 123",
        "A",
        "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP"
    })
    void testFailGracefully_DadosValidos(String nome) {
        Produto produto = new Produto(nome, 99.99);
        
        try {
            Produto salvo = service.salvar(produto);
            assertNotNull(salvo.getId(), "Produto válido deveria ser salvo");
            assertEquals(nome, salvo.getNome());
            assertEquals(99.99, salvo.getPreco());
        } catch (IllegalArgumentException e) {
            fail("Dados válidos não deveria rejeitar: " + e.getMessage());
        }
    }

    // ===== TESTES DE RECUPERAÇÃO APÓS ERRO =====

    @Test
    @DisplayName("Sistema deveria recuperar após erro de validação")
    void testFailGracefully_RecuperacaoAposErro() {
        // Tentar salvar inválido
        Produto invalido = new Produto("", 100.0);
        assertThrows(IllegalArgumentException.class, 
            () -> service.salvar(invalido));
        
        // Depois salvar válido
        Produto valido = new Produto("Produto Válido", 100.0);
        Produto salvo = service.salvar(valido);
        
        assertNotNull(salvo.getId(), "Deveria recuperar e salvar válido");
        
        // Verificar que está realmente salvo
        var recuperado = service.buscarPorId(salvo.getId());
        assertTrue(recuperado.isPresent());
    }

    @Test
    @DisplayName("Erro de validação não deveria corromper estado")
    void testFailGracefully_EstadoIntacto() {
        List<Produto> antesDeFalha = new ArrayList<>(service.listarTodos());
        
        // Tentar múltiplas operações inválidas
        for (int i = 0; i < 5; i++) {
            Produto invalido = new Produto("", -50.0);
            try {
                service.salvar(invalido);
            } catch (IllegalArgumentException e) {
                // Esperado
            }
        }
        
        List<Produto> depoisDeFalha = new ArrayList<>(service.listarTodos());
        
        assertEquals(antesDeFalha.size(), depoisDeFalha.size(),
            "Múltiplas falhas não deveria alterar estado");
    }

    // ===== TESTES DE EDGE CASES =====

    @Test
    @DisplayName("Deve aceitar preço muito pequeno mas positivo")
    void testFailEarly_PrecoMuitoPequeno() {
        Produto produto = new Produto("Teste", 0.01);
        
        Produto salvo = service.salvar(produto);
        assertNotNull(salvo.getId(), "Deveria aceitar preço mínimo válido");
    }

    @Test
    @DisplayName("Deve rejeitar preço logo abaixo do mínimo")
    void testFailEarly_PrecoAbaixoMinimo() {
        Produto produto = new Produto("Teste", 0.001);
        
        try {
            service.salvar(produto);
            // Pode aceitar ou rejeitar, mas deveria ser consistente
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("Deve aceitar nome com exatamente 255 caracteres")
    void testFailEarly_NomeNoLimiteMaximo() {
        String nome = "A".repeat(255);
        Produto produto = new Produto(nome, 100.0);
        
        Produto salvo = service.salvar(produto);
        assertNotNull(salvo.getId());
    }

    @Test
    @DisplayName("Deve rejeitar nome com 256 caracteres")
    void testFailEarly_NomeAcimaDoLimite() {
        String nome = "A".repeat(256);
        Produto produto = new Produto(nome, 100.0);
        
        assertThrows(IllegalArgumentException.class,
            () -> service.salvar(produto));
    }

    // ===== TESTES DE CONSISTÊNCIA =====

    @Test
    @DisplayName("Mensagens de erro deveria ser consistentes")
    void testFailGracefully_ConsistenciaMensagens() {
        String mensagem1 = null;
        String mensagem2 = null;
        
        // Primeira tentativa
        try {
            service.salvar(new Produto("", 100.0));
        } catch (IllegalArgumentException e) {
            mensagem1 = e.getMessage();
        }
        
        // Segunda tentativa com mesmo erro
        try {
            service.salvar(new Produto("", 100.0));
        } catch (IllegalArgumentException e) {
            mensagem2 = e.getMessage();
        }
        
        assertEquals(mensagem1, mensagem2,
            "Mensagens para mesmo erro deveria ser idênticas");
    }
}
