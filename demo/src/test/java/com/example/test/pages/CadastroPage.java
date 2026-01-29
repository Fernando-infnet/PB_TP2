package com.example.test.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model para a página de Cadastro (Criar/Editar).
 * Encapsula todas as interações com o formulário de produto.
 */
public class CadastroPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Localizadores
    private final By inputNome = By.id("nome");
    private final By inputPreco = By.id("preco");
    private final By botaoSalvar = By.id("btnSalvar");
    private final By botaoCancelar = By.id("btnCancelar");
    private final By inputId = By.id("inputId");
    private final By mensagemErro = By.cssSelector(".alert-danger");

    public CadastroPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    /**
     * Digita o nome do produto
     */
    public void digitarNome(String nome) {
        driver.findElement(inputNome).clear();
        driver.findElement(inputNome).sendKeys(nome);
    }

    /**
     * Digita o preço do produto
     */
    public void digitarPreco(String preco) {
        driver.findElement(inputPreco).clear();
        driver.findElement(inputPreco).sendKeys(preco);
    }

    /**
     * Clica no botão salvar
     */
    public void clicarSalvar() {
        driver.findElement(botaoSalvar).click();
    }

    /**
     * Clica no botão cancelar
     */
    public void clicarCancelar() {
        driver.findElement(botaoCancelar).click();
    }

    /**
     * Preenche o formulário e envia
     */
    public void preencherFormulario(String nome, String preco) {
        digitarNome(nome);
        digitarPreco(preco);
        clicarSalvar();
    }

    /**
     * Obtém a URL atual
     */
    public String obterUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Verifica se o campo de nome está visível
     */
    public boolean isCampoNomeVisivel() {
        try {
            return driver.findElement(inputNome).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se o campo de preço está visível
     */
    public boolean isCampoPrecoVisivel() {
        try {
            return driver.findElement(inputPreco).isDisplayed();
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
     * Verifica se o formulário está em modo de edição (campo ID possui valor)
     */
    public boolean estaEmModoEdicao() {
        String idValue = driver.findElement(inputId).getAttribute("value");
        return idValue != null && !idValue.isEmpty();
    }

    /**
     * Obtém o valor atual do nome no formulário
     */
    public String obterValorNome() {
        return driver.findElement(inputNome).getAttribute("value");
    }

    /**
     * Obtém o valor atual do preço no formulário
     */
    public String obterValorPreco() {
        return driver.findElement(inputPreco).getAttribute("value");
    }

    /**
     * Limpa todos os campos do formulário
     */
    public void limparFormulario() {
        driver.findElement(inputNome).clear();
        driver.findElement(inputPreco).clear();
    }
}
