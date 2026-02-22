/**
 * Testes para DataProvider (Testes Parametrizados)
 * Cobrindo múltiplos cenários com diferentes combinações de dados
 */
package com.example.test;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.example.test.base.BaseTest;
import com.example.test.pages.CadastroPage;
import com.example.test.pages.ListarPage;

public class CadastroTestParametrizado extends BaseTest {

    /**
     * DataProvider para teste de cadastro parametrizado
     * Fornece múltiplas combinações de dados de entrada
     */
    @DataProvider(name = "dadosRegistroProduto")
    public Object[][] dadosRegistroProduto() {
        return new Object[][] {
            // { nome, preco, descricao, esperado }
            
            // ===== CASOS VÁLIDOS (esperado = true) =====
            { "Notebook Dell", "1500.00", "Nome e preço válidos", true },
            { "Mouse Logitech", "99.99", "Preço com centavos", true },
            { "Teclado Mecânico", "350.00", "Preço redondo", true },
            { "Monitor LG", "899.90", "Preço decimal", true },
            { "A", "0.01", "Tamanho mínimo de nome e preço", true },
            { "Produto com Espaços Válidos", "1000.00", "Nome com múltiplos espaços", true },
            
            // ===== CASOS INVÁLIDOS - NOME (esperado = false) =====
            { "", "100.00", "Nome vazio", false },
            { "   ", "100.00", "Nome com apenas espaços", false },
            { null, "100.00", "Nome nulo", false },
            
            // ===== CASOS INVÁLIDOS - PREÇO (esperado = false) =====
            { "Produto", "-50.00", "Preço negativo", false },
            { "Produto", "0.00", "Preço zero", false },
            { "Produto", "-0.01", "Preço negativo pequeno", false },
            { "Produto", null, "Preço nulo", false },
            { "Produto", "abc", "Preço inválido (texto)", false },
            
            // ===== CASOS LIMITES =====
            { "P".repeat(255), "99.99", "Nome no limite máximo (255 chars)", true },
            { "P".repeat(256), "99.99", "Nome acima do limite (256 chars)", false },
            { "Produto", "999999.99", "Preço muito alto", true },
            { "Produto", "1000000.00", "Preço no limite superior", false },
        };
    }

    @Test(dataProvider = "dadosRegistroProduto", 
        description = "Teste parametrizado para cadastro de produto com múltiplos cenários")
    public void testRegistroProdutoParametrizado(String nome, String preco, String descricao, boolean esperado) {
        System.out.println("\n=== Testando: " + descricao + " ===");
        System.out.println("Nome: " + nome + " | Preço: " + preco + " | Esperado: " + esperado);
        
        try {
            driver.get(BASE_URL + "/produtos/cadastrar");
            CadastroPage page = new CadastroPage(driver, wait);
            
            // Preencher e submeter apenas se nome e preço não forem nulos
            if (nome != null && preco != null) {
                page.preencherFormulario(nome, preco);
            } else if (nome != null) {
                page.digitarNome(nome);
                page.clicarSalvar();
            } else {
                page.clicarSalvar();  // Submeter vazio
            }
            
            // Verificar resultado esperado
            if (esperado) {
                Assert.assertTrue(driver.getCurrentUrl().contains("/listar"), 
                    "Deveria redirecionar para lista após sucesso: " + descricao);
                
                ListarPage listarPage = new ListarPage(driver, wait);
                Assert.assertTrue(listarPage.isMensagemSucessoVisivel(),
                    "Deve exibir mensagem de sucesso: " + descricao);
            } else {
                Assert.assertTrue(
                    driver.getCurrentUrl().contains("/cadastrar") || page.isMensagemErroVisivel(),
                    "Deveria falhar ou exibir erro: " + descricao
                );
            }
        } catch (Exception e) {
            if (!esperado) {
                // Exceção esperada para dados inválidos
                System.out.println("Exceção esperada: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * DataProvider para teste de edição parametrizado
     */
    @DataProvider(name = "dadosEdicao")
    public Object[][] dadosEdicao() {
        return new Object[][] {
            // { idProduto, novoNome, novoPreco, esperado }
            { 1L, "Notebook Atualizado", "1800.00", true },
            { 1L, "Monitor Samsung", "1200.00", true },
            { 1L, "", "1800.00", false },  // Nome vazio
            { 1L, "Produto", "-100.00", false },  // Preço negativo
            { 999L, "Qualquer", "100.00", false },  // ID inexistente
            { 1L, "P".repeat(256), "100.00", false },  // Nome muito longo
        };
    }

    @Test(dataProvider = "dadosEdicao",
        description = "Teste parametrizado para edição de produto")
    public void testEditarProdutoParametrizado(Long idProduto, String novoNome, String novoPreco, boolean esperado) {
        System.out.println("\n=== Editando produto ID: " + idProduto + " ===");
        
        try {
            driver.get(BASE_URL + "/produtos/editar/" + idProduto);
            
            // Verificar se produto foi encontrado
            if (!driver.getCurrentUrl().contains("/editar/")) {
                Assert.assertFalse(esperado, "Produto não encontrado (esperado)");
                return;
            }
            
            CadastroPage page = new CadastroPage(driver, wait);
            page.preencherFormulario(novoNome, novoPreco);
            
            if (esperado) {
                Assert.assertTrue(driver.getCurrentUrl().contains("/listar"),
                    "Deveria atualizar e redirecionar");
            } else {
                Assert.assertTrue(
                    driver.getCurrentUrl().contains("/editar") || page.isMensagemErroVisivel(),
                    "Deveria falhar na validação"
                );
            }
        } catch (Exception e) {
            if (!esperado) {
                System.out.println("Exceção esperada: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * DataProvider para validações de preço específicas
     */
    @DataProvider(name = "formatosPreco")
    public Object[][] formatosPreco() {
        return new Object[][] {
            { "99.99", true },       // Válido
            { "100", true },         // Inteiro é válido
            { "1000.5", true },      // Uma casa decimal
            { "0.01", true },        // Mínimo válido
            { "-50.00", false },     // Negativo
            { "0", false },          // Zero
            { "abc", false },        // Texto
            { "99,99", false },      // Vírgula (locale)
            { "1.000,00", false },   // Formato brasileiro
            { "", false },           // Vazio
            { "   ", false },        // Espaços
        };
    }

    @Test(dataProvider = "formatosPreco",
        description = "Teste de validação de diferentes formatos de preço")
    public void testValidacaoFormatoPreco(String preco, boolean esperado) {
        System.out.println("Testando preço: '" + preco + "' (esperado válido: " + esperado + ")");
        
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.digitarNome("Produto Teste");
        page.digitarPreco(preco);
        page.clicarSalvar();
        
        if (esperado) {
            Assert.assertTrue(driver.getCurrentUrl().contains("/listar"),
                "Preço " + preco + " deveria ser aceito");
        } else {
            Assert.assertTrue(
                driver.getCurrentUrl().contains("/cadastrar") || page.isMensagemErroVisivel(),
                "Preço " + preco + " deveria ser rejeitado"
            );
        }
    }
}
