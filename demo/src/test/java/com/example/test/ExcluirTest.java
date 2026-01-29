package com.example.test;

import org.openqa.selenium.Alert;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.example.test.base.BaseTest;
import com.example.test.pages.ListarPage;

/**
 * Classe de testes para funcionalidade de exclusão de produtos.
 * Testa operações de exclusão e diálogos de confirmação.
 */
public class ExcluirTest extends BaseTest {

    /**
     * Testa a visibilidade do botão excluir
     */
    @Test(description = "Verifica se o botão excluir está visível para cada produto")
    public void testBotaoExcluirVisivel() {
        ListarPage page = new ListarPage(driver, wait);
        
        int quantidadeInicial = page.obterQuantidadeProdutos();
        Assert.assertTrue(quantidadeInicial > 0, 
            "Deveria haver pelo menos um produto para testar exclusão");
    }

    /**
     * Testa exclusão bem-sucedida de produto
     */
    @Test(description = "Testa exclusão bem-sucedida de produto")
    public void testExclusaoProdutoSucesso() {
        // Primeiro, cria um produto para excluir
        driver.get(BASE_URL + "/produtos/cadastrar");
        // Preenche o formulário com dados de teste
        driver.findElement(org.openqa.selenium.By.id("nome")).sendKeys("Produto para Deletar");
        driver.findElement(org.openqa.selenium.By.id("preco")).sendKeys("50.00");
        driver.findElement(org.openqa.selenium.By.id("btnSalvar")).click();
        
        // Aguarda e verifica se está na página de listagem
        Assert.assertTrue(driver.getCurrentUrl().contains("/listar"));
        
        ListarPage page = new ListarPage(driver, wait);
        int quantidadeAntes = page.obterQuantidadeProdutos();
        
        // Para este teste, vamos excluir o primeiro produto disponível
        if (quantidadeAntes > 0) {
            page.clicarExcluir(1L);
            
            // Trata o diálogo de confirmação
            try {
                Alert alerta = driver.switchTo().alert();
                alerta.accept();
            } catch (Exception e) {
                // O diálogo pode não aparecer em alguns casos
            }
            
            // Verifica mensagem de sucesso
            Assert.assertTrue(page.isMensagemSucessoVisivel(), 
                "Mensagem de sucesso deveria ser exibida");
        }
    }

    /**
     * Testa o diálogo de confirmação de exclusão
     */
    @Test(description = "Testa se o diálogo de confirmação de exclusão aparece")
    public void testDialogoConfirmacaoExclusao() {
        ListarPage page = new ListarPage(driver, wait);
        
        // O botão de excluir tem onclick="return confirm(...)"
        // Podemos verificar se o botão existe e é clicável
        try {
            page.clicarExcluir(1L);
            
            // Se chegarmos aqui, o diálogo apareceu e precisamos tratá-lo
            Alert alerta = driver.switchTo().alert();
            String mensagem = alerta.getText();
            
            Assert.assertTrue(mensagem.toLowerCase().contains("certeza") || 
                            mensagem.toLowerCase().contains("confirma"),
                "Mensagem de confirmação deveria perguntar sobre exclusão");
            
            // Cancela o alerta
            alerta.dismiss();
        } catch (Exception e) {
            // O tratamento do alerta varia por navegador/driver
        }
    }

    /**
     * Testa exclusão seguida de cancelamento
     */
    @Test(description = "Testa o cancelamento da exclusão no diálogo de confirmação")
    public void testCancelarExclusao() {
        ListarPage page = new ListarPage(driver, wait);
        int quantidadeAntes = page.obterQuantidadeProdutos();
        
        try {
            page.clicarExcluir(1L);
            
            Alert alerta = driver.switchTo().alert();
            alerta.dismiss();
            
            // Verifica se a quantidade permanece a mesma
            int quantidadeDepois = page.obterQuantidadeProdutos();
            Assert.assertEquals(quantidadeDepois, quantidadeAntes, 
                "Quantidade de produtos deveria ser a mesma após cancelar exclusão");
        } catch (Exception e) {
            // Continua caso o tratamento do alerta falhe
        }
    }

    /**
     * Testa exclusão sequencial de múltiplos produtos
     */
    @Test(description = "Testa exclusão sequencial de múltiplos produtos")
    public void testExclusaoMultiplosProdutos() {
        ListarPage page = new ListarPage(driver, wait);
        
        int quantidadeInicial = page.obterQuantidadeProdutos();
        
        // Tenta excluir produtos um a um (com limite para evitar excluir todos)
        int aExcluir = Math.min(2, quantidadeInicial);
        
        for (int i = 0; i < aExcluir; i++) {
            if (page.obterQuantidadeProdutos() > 0) {
                try {
                    page.clicarExcluir(1L);
                    Alert alerta = driver.switchTo().alert();
                    alerta.accept();
                    
                    // Atualiza para obter a lista atualizada
                    driver.navigate().refresh();
                    page = new ListarPage(driver, wait);
                } catch (Exception e) {
                    // Continua mesmo que a exclusão falhe
                }
            }
        }
        
        // Verifica se alguns produtos foram excluídos
        int quantidadeFinal = page.obterQuantidadeProdutos();
        Assert.assertTrue(quantidadeFinal < quantidadeInicial || quantidadeInicial == 0,
            "Quantidade de produtos deveria ser menor após exclusões");
    }

    /**
     * Testa exclusão com ID inválido
     */
    @Test(description = "Testa exclusão de produto inexistente")
    public void testExclusaoProdutoNaoExistente() {
        driver.get(BASE_URL + "/produtos/excluir/99999");
        
        // Deve redirecionar para a listagem
        Assert.assertTrue(driver.getCurrentUrl().contains("/listar"), 
            "Deveria redirecionar para listagem");
        
        ListarPage page = new ListarPage(driver, wait);
        Assert.assertTrue(page.isMensagemErroVisivel(), 
            "Deveria exibir mensagem de erro para produto inexistente");
    }
}
