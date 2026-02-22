package com.example.test.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model para a página de Listagem.
 * Encapsula todas as interações com a página de lista de produtos.
 */
public class ListarPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Localizadores
    private final By tabela = By.id("produtosTable");
    private final By botaoCadastrar = By.id("btnCadastrarNovo");
    private final By linhasProduto = By.xpath("//table[@id='produtosTable']/tbody/tr[not(td[contains(text(), 'Nenhum')])]");
    private final By mensagemSucesso = By.cssSelector(".alert-success");
    private final By mensagemErro = By.cssSelector(".alert-danger");

    public ListarPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    /**
     * Verifica se a tabela está visível
     */
    public boolean isTabelaVisivel() {
        try {
            return driver.findElement(tabela).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtém a quantidade de produtos exibidos
     */
    public int obterQuantidadeProdutos() {
        List<WebElement> linhas = driver.findElements(linhasProduto);
        return linhas.size();
    }

    /**
     * Clica no botão "Cadastrar Novo"
     */
    public void clicarCadastrarNovo() {
        driver.findElement(botaoCadastrar).click();
    }

    /**
     * Clica no botão editar para um ID específico de produto
     */
    public void clicarEditar(Long produtoId) {
        By btnEditar = By.id("btnEditar-" + produtoId);
        driver.findElement(btnEditar).click();
    }

    /**
     * Clica no botão excluir para um ID específico de produto
     */
    public void clicarExcluir(Long produtoId) {
        By btnExcluir = By.id("btnExcluir-" + produtoId);
        driver.findElement(btnExcluir).click();
    }

    /**
     * Clica no botão editar do primeiro produto na tabela
     */
    public void clicarEditarPrimeiroProduto() {
        By btnEditarPrimeiro = By.xpath("//table[@id='produtosTable']/tbody/tr[1]//button[contains(@id, 'btnEditar')]");
        driver.findElement(btnEditarPrimeiro).click();
    }

    /**
     * Clica no botão excluir do último produto na tabela
     */
    public void clicarExcluirUltimoProduto() {
        By btnExcluirUltimo = By.xpath("//table[@id='produtosTable']/tbody/tr[last()]//button[contains(@id, 'btnExcluir')]");
        driver.findElement(btnExcluirUltimo).click();
    }

    /**
     * Verifica se a mensagem de sucesso está visível
     */
    public boolean isMensagemSucessoVisivel() {
        try {
            return driver.findElement(mensagemSucesso).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se a mensagem de erro está visível
     */
    public boolean isMensagemErroVisivel() {
        try {
            return driver.findElement(mensagemErro).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtém o texto da mensagem de sucesso
     */
    public String obterTextoMensagemSucesso() {
        try {
            return driver.findElement(mensagemSucesso).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Obtém o texto da mensagem de erro
     */
    public String obterTextoMensagemErro() {
        try {
            return driver.findElement(mensagemErro).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Verifica se o produto existe na tabela pelo ID
     */
    public boolean produtoExisteNaTabela(Long produtoId) {
        try {
            By produtoRow = By.id("produto-" + produtoId);
            return driver.findElement(produtoRow).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtém o nome do produto na tabela
     */
    public String obterNomeProduto(Long produtoId) {
        By produtoRow = By.id("produto-" + produtoId);
        List<WebElement> celulas = driver.findElement(produtoRow).findElements(By.tagName("td"));
        return celulas.size() > 1 ? celulas.get(1).getText() : "";
    }

    /**
     * Obtém o preço do produto na tabela
     */
    public String obterPrecoProduto(Long produtoId) {
        By produtoRow = By.id("produto-" + produtoId);
        List<WebElement> celulas = driver.findElement(produtoRow).findElements(By.tagName("td"));
        return celulas.size() > 2 ? celulas.get(2).getText() : "";
    }
}
