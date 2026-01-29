package com.example.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.example.test.base.BaseTest;
import com.example.test.pages.ListarPage;

/**
 * Classe de testes para funcionalidade de listagem de produtos.
 * Testa a exibição e interação com a página de listagem de produtos.
 */
public class ListarTest extends BaseTest {

    /**
     * Testa se a tabela de produtos está visível ao carregar a página
     */
    @Test(description = "Verifica se a tabela de produtos está visível ao carregar a página")
    public void testTabelaProdutosVisivel() {
        ListarPage page = new ListarPage(driver, wait);
        Assert.assertTrue(page.isTabelaVisivel(), "Tabela de produtos não está visível");
    }

    /**
     * Testa se o botão cadastrar é clicável e navega para o formulário
     */
    @Test(description = "Verifica se clicar no botão 'Cadastrar Novo' navega para a página de formulário")
    public void testCadastrarNovoNavegacao() {
        ListarPage page = new ListarPage(driver, wait);
        page.clicarCadastrarNovo();
        Assert.assertTrue(driver.getCurrentUrl().contains("/cadastrar"), 
            "Navegação para página de cadastro falhou");
    }

    /**
     * Testa se ao menos um produto é exibido inicialmente
     */
    @Test(description = "Verifica se pelo menos um produto é exibido na listagem")
    public void testListaProdutosNaoVazia() {
        ListarPage page = new ListarPage(driver, wait);
        int quantidadeProdutos = page.obterQuantidadeProdutos();
        Assert.assertTrue(quantidadeProdutos > 0, 
            "Lista de produtos está vazia, deveria conter produtos iniciais");
    }

    /**
     * Testa a exibição da mensagem de sucesso
     */
    @Test(description = "Verifica se a mensagem de sucesso não está visível inicialmente")
    public void testMensagemSucessoNaoVisivel() {
        ListarPage page = new ListarPage(driver, wait);
        Assert.assertFalse(page.isMensagemSucessoVisivel(), 
            "Mensagem de sucesso não deveria estar visível ao carregar página");
    }

    /**
     * Testa a exibição dos dados do produto na tabela
     */
    @Test(description = "Verifica se o nome do produto é exibido corretamente na listagem")
    public void testDadosProdutoExibidos() {
        ListarPage page = new ListarPage(driver, wait);
        String nomeProduto = page.obterNomeProduto(1L);
        Assert.assertNotNull(nomeProduto, "Nome do produto não deveria ser nulo");
        Assert.assertFalse(nomeProduto.isEmpty(), "Nome do produto não deveria estar vazio");
    }

    /**
     * Testa o formato de exibição do preço na tabela
     */
    @Test(description = "Verifica se o preço do produto é exibido com formato de moeda")
    public void testPrecoProdutoExibido() {
        ListarPage page = new ListarPage(driver, wait);
        String precoProduto = page.obterPrecoProduto(1L);
        Assert.assertNotNull(precoProduto, "Preço do produto não deveria ser nulo");
        Assert.assertTrue(precoProduto.contains("R$"), 
            "Preço deveria estar no formato com símbolo R$");
    }
}
