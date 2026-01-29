package com.example.test;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.example.test.base.BaseTest;
import com.example.test.pages.CadastroPage;
import com.example.test.pages.ListarPage;

/**
 * Classe de testes para funcionalidade de cadastro de produtos.
 * Testa cenários positivos e negativos para criação de produtos.
 */
public class CadastroTest extends BaseTest {

    /**
     * Testa a navegação para a página de cadastro
     */
    @Test(description = "Verifica a navegação para a página de cadastro de produto")
    public void testNavegacaoCadastro() {
        ListarPage listarPage = new ListarPage(driver, wait);
        listarPage.clicarCadastrarNovo();
        Assert.assertTrue(driver.getCurrentUrl().contains("/cadastrar"), 
            "URL deveria conter /cadastrar");
    }

    /**
     * Testa a visibilidade dos campos do formulário
     */
    @Test(description = "Verifica se os campos do formulário estão visíveis na página de cadastro")
    public void testCamposFormularioVisiveis() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        Assert.assertTrue(page.isCampoNomeVisivel(), "Campo de nome não está visível");
        Assert.assertTrue(page.isCampoPrecoVisivel(), "Campo de preço não está visível");
    }

        /**
         * Teste parametrizado para cadastros válidos e inválidos de produtos
         */
        @Test(dataProvider = "dadosRegistroProduto", 
            description = "Teste parametrizado para cadastro de produto com dados variados")
    public void testRegistroProdutoParametrizado(String nome, String preco, boolean esperado) {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.preencherFormulario(nome, preco);
        
        if (esperado) {
            // Deve navegar para a página de listagem em caso de sucesso
            Assert.assertTrue(driver.getCurrentUrl().contains("/listar"), 
                "Deveria redirecionar para página de listagem após sucesso");
        } else {
            // Deve permanecer no formulário ou exibir erro para dados inválidos
            Assert.assertTrue(
                driver.getCurrentUrl().contains("/cadastrar") || page.isMensagemErroVisivel(),
                "Deveria manter na página de formulário ou exibir erro para dados inválidos"
            );
        }
    }

    /**
     * Testa cadastro de produto com sucesso
     */
    @Test(description = "Testa cadastro de produto com dados válidos")
    public void testRegistroProdutoSucesso() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        String nomeProduto = "Produto Teste " + System.currentTimeMillis();
        page.preencherFormulario(nomeProduto, "99.99");
        
        // Aguarda redirecionamento para a página de listagem
        Assert.assertTrue(driver.getCurrentUrl().contains("/listar"), 
            "Deveria redirecionar para página de listagem");
        
        // Verifica se a mensagem de sucesso foi exibida
        ListarPage listarPage = new ListarPage(driver, wait);
        Assert.assertTrue(listarPage.isMensagemSucessoVisivel(), 
            "Mensagem de sucesso deveria ser exibida");
    }

    /**
     * Testa cadastro com nome vazio
     */
    @Test(description = "Testa cadastro com nome de produto vazio (teste negativo)")
    public void testRegistroSemNome() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.digitarNome("");
        page.digitarPreco("10.00");
        page.clicarSalvar();
        
        // Deve permanecer no formulário ou exibir erro
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/cadastrar") || page.isMensagemErroVisivel(),
            "Registro sem nome deveria falhar"
        );
    }

    /**
     * Testa cadastro com preço negativo
     */
    @Test(description = "Testa cadastro com preço negativo (teste negativo)")
    public void testRegistroComPrecoNegativo() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.preencherFormulario("Produto Teste", "-5.00");
        
        // Deve falhar - com mensagem de erro ou permanecendo no formulário
        Assert.assertTrue(
            page.isMensagemErroVisivel() || driver.getCurrentUrl().contains("/cadastrar"),
            "Registro com preço negativo deveria falhar"
        );
    }

    /**
     * Data provider para testes parametrizados
     * Inclui cenários válidos e inválidos
     */
    @DataProvider(name = "dadosRegistroProduto")
    public Object[][] dadosRegistroProduto() {
        return new Object[][] {
            // {nome, preco, esperado_sucesso}
            {"Produto Válido 1", "10.50", true},        // Positivo: produto válido
            {"Produto Válido 2", "99.99", true},        // Positivo: produto válido
            {"Monitores", "1200.00", true},             // Positivo: produto válido
            {"", "10.50", false},                       // Negativo: nome vazio
            {"Produto Inválido", "-5.00", false},       // Negativo: preço negativo
            {"Produto Inválido", "abc", false},         // Negativo: formato de preço inválido
        };
    }
}
