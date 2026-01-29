package com.example.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.example.test.base.BaseTest;
import com.example.test.pages.CadastroPage;
import com.example.test.pages.ListarPage;

/**
 * Classe de testes para funcionalidade de edição de produtos.
 * Testa operações de atualização e pré-preenchimento do formulário com dados existentes.
 */
public class EditarTest extends BaseTest {

    /**
     * Testa a navegação para a página de edição
     */
    @Test(description = "Verifica a navegação para a página de edição de produto")
    public void testNavegacaoEdicao() {
        ListarPage page = new ListarPage(driver, wait);
        // Edita o primeiro produto (ID 1)
        page.clicarEditar(1L);
        Assert.assertTrue(driver.getCurrentUrl().contains("/editar"), 
            "URL deveria conter /editar");
    }

    /**
     * Testa se o formulário é preenchido com dados existentes
     */
    @Test(description = "Verifica se o formulário de edição é pré-preenchido com dados do produto")
    public void testFormularioPreaPreenchido() {
        driver.get(BASE_URL + "/produtos/editar/1");
        CadastroPage page = new CadastroPage(driver, wait);
        
        // Verifica se o formulário está em modo de edição
        Assert.assertTrue(page.estaEmModoEdicao(), 
            "Formulário deveria estar em modo de edição");
        
        // Verifica se os campos possuem valores
        String nome = page.obterValorNome();
        String preco = page.obterValorPreco();
        
        Assert.assertFalse(nome.isEmpty(), "Campo de nome deveria estar preenchido");
        Assert.assertFalse(preco.isEmpty(), "Campo de preço deveria estar preenchido");
    }

    /**
     * Testa atualização bem-sucedida do produto
     */
    @Test(description = "Testa atualização do produto com dados modificados")
    public void testAtualizacaoProdutoSucesso() {
        driver.get(BASE_URL + "/produtos/editar/1");
        CadastroPage page = new CadastroPage(driver, wait);
        
        String novoNome = "Produto Atualizado " + System.currentTimeMillis();
        String novoPreco = "199.99";
        
        page.limparFormulario();
        page.preencherFormulario(novoNome, novoPreco);
        
        // Deve redirecionar para a página de listagem
        Assert.assertTrue(driver.getCurrentUrl().contains("/listar"), 
            "Deveria redirecionar para página de listagem após atualização");
        
        // Verifica mensagem de sucesso
        ListarPage listarPage = new ListarPage(driver, wait);
        Assert.assertTrue(listarPage.isMensagemSucessoVisivel(), 
            "Mensagem de sucesso deveria ser exibida");
    }

    /**
     * Testa atualização com nome vazio
     */
    @Test(description = "Testa atualização com nome de produto vazio (teste negativo)")
    public void testAtualizacaoSemNome() {
        driver.get(BASE_URL + "/produtos/editar/1");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.limparFormulario();
        page.digitarNome("");
        page.digitarPreco("50.00");
        page.clicarSalvar();
        
        // Deve permanecer no formulário ou exibir erro
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/editar") || page.isMensagemErroVisivel(),
            "Atualização sem nome deveria falhar"
        );
    }

    /**
     * Testa atualização com preço negativo
     */
    @Test(description = "Testa atualização com preço negativo (teste negativo)")
    public void testAtualizacaoComPrecoNegativo() {
        driver.get(BASE_URL + "/produtos/editar/1");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.limparFormulario();
        page.preencherFormulario("Produto Teste", "-10.00");
        
        // Deve falhar
        Assert.assertTrue(
            page.isMensagemErroVisivel() || driver.getCurrentUrl().contains("/editar"),
            "Atualização com preço negativo deveria falhar"
        );
    }

    /**
     * Testa o botão cancelar no modo de edição
     */
    @Test(description = "Testa se o botão cancelar retorna para a listagem sem salvar alterações")
    public void testCancelarEdicao() {
        driver.get(BASE_URL + "/produtos/editar/1");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.clicarCancelar();
        
        Assert.assertTrue(driver.getCurrentUrl().contains("/listar"), 
            "Deveria redirecionar para página de listagem ao cancelar");
    }

    /**
     * Testa edição de produto inexistente
     */
    @Test(description = "Testa acesso à página de edição de produto inexistente")
    public void testEdicaoProdutoNaoExistente() {
        driver.get(BASE_URL + "/produtos/editar/99999");
        
        // Deve redirecionar para a listagem com erro
        Assert.assertTrue(driver.getCurrentUrl().contains("/listar"), 
            "Deveria redirecionar para listagem ao tentar editar produto inexistente");
        
        ListarPage page = new ListarPage(driver, wait);
        Assert.assertTrue(page.isMensagemErroVisivel(), 
            "Mensagem de erro deveria ser exibida");
    }
}
