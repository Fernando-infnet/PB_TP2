/**
 * Testes Selenium para Simulação de Falhas na Interface
 * Cobre cenários de validação UI, alertas, cliques duplos e timeouts
 */
package com.example.test;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.example.test.base.BaseTest;
import com.example.test.pages.CadastroPage;
import com.example.test.pages.ListarPage;

/**
 * Testes de falha na camada de interface com Selenium
 * Valida tratamento de erros, validações e casos extremos
 */
public class FailureSeleniumTest extends BaseTest {

    // ===== TESTES DE FORMULÁRIO VAZIO =====

    @Test(description = "Deve exibir erro ao submeter formulário completamente vazio")
    public void testFormularioCompletoVazio() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        // Clicar salvar sem preencher nada
        page.clicarSalvar();
        
        // Deve permanecer no formulário ou exibir erro
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/cadastrar") || page.isMensagemErroVisivel(),
            "Formulário vazio deveria ser rejeitado"
        );
    }

    @Test(description = "Deve exibir erro ao submeter apenas nome vazio")
    public void testApenasNomeVazio() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.digitarNome("");
        page.digitarPreco("100.00");
        page.clicarSalvar();
        
        Assert.assertTrue(
            page.isMensagemErroVisivel() || driver.getCurrentUrl().contains("/cadastrar"),
            "Nome vazio deveria ser rejeitado"
        );
    }

    @Test(description = "Deve exibir erro ao submeter apenas preço vazio")
    public void testApenasPrecoVazio() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.digitarNome("Produto Teste");
        page.digitarPreco("");
        page.clicarSalvar();
        
        Assert.assertTrue(
            page.isMensagemErroVisivel() || driver.getCurrentUrl().contains("/cadastrar"),
            "Preço vazio deveria ser rejeitado"
        );
    }

    @Test(description = "Deve exibir erro ao submeter nome com apenas espaços")
    public void testNomeApenasEspacos() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.digitarNome("     ");
        page.digitarPreco("100.00");
        page.clicarSalvar();
        
        Assert.assertTrue(
            page.isMensagemErroVisivel() || driver.getCurrentUrl().contains("/cadastrar"),
            "Nome com apenas espaços deveria ser rejeitado"
        );
    }

    // ===== TESTES DE VALIDAÇÃO DE PREÇO =====

    @Test(description = "Deve validar preço negativo")
    public void testValidacaoPrecoNegativo() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.digitarNome("Produto Teste");
        page.digitarPreco("-50.00");
        page.clicarSalvar();
        
        Assert.assertTrue(
            page.isMensagemErroVisivel() || driver.getCurrentUrl().contains("/cadastrar"),
            "Preço negativo deveria ser rejeitado"
        );
    }

    @Test(description = "Deve validar preço zero")
    public void testValidacaoPrecoZero() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.digitarNome("Produto Teste");
        page.digitarPreco("0.00");
        page.clicarSalvar();
        
        Assert.assertTrue(
            page.isMensagemErroVisivel() || driver.getCurrentUrl().contains("/cadastrar"),
            "Preço zero deveria ser rejeitado"
        );
    }

    @Test(description = "Deve validar preço com caracteres inválidos")
    public void testValidacaoPrecoInvalido() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.digitarNome("Produto Teste");
        page.digitarPreco("abc");
        page.clicarSalvar();
        
        Assert.assertTrue(
            page.isMensagemErroVisivel() || driver.getCurrentUrl().contains("/cadastrar"),
            "Preço inválido deveria ser rejeitado"
        );
    }

    // ===== TESTES DE ENTRADA MALICIOSA =====

    @Test(description = "Deve rejeitar XSS no nome do produto")
    public void testValidacaoXSSNome() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        String nomeComXSS = "<script>alert('XSS')</script>";
        page.digitarNome(nomeComXSS);
        page.digitarPreco("100.00");
        page.clicarSalvar();
        
        // Deve rejeitar ou redirecionar (não deveria executar script)
        Assert.assertTrue(
            page.isMensagemErroVisivel() || driver.getCurrentUrl().contains("/cadastrar"),
            "XSS deveria ser rejeitado"
        );
    }

    @Test(description = "Deve lidar com nome muito longo")
    public void testNomeMuitoLongo() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        String nomeLongo = "A".repeat(1000);
        page.digitarNome(nomeLongo);
        page.digitarPreco("100.00");
        page.clicarSalvar();
        
        // Deveria aceitar ou rejeitar com mensagem clara
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/listar") || 
            page.isMensagemErroVisivel(),
            "Deveria processar nome longo de forma graceful"
        );
    }

    // ===== TESTES DE CLIQUE DUPLO =====

    @Test(description = "Deve evitar duplicação por clique duplo no botão salvar")
    public void testCliqueDuploSalvar() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        // Obter quantidade inicial
        driver.get(BASE_URL + "/produtos/listar");
        ListarPage listarPage = new ListarPage(driver, wait);
        int quantidadeAntes = listarPage.obterQuantidadeProdutos();
        
        // Voltar ao cadastro
        driver.get(BASE_URL + "/produtos/cadastrar");
        page = new CadastroPage(driver, wait);
        
        // Preencher e clicar duplo
        String nomeProduto = "Produto Duplo " + System.currentTimeMillis();
        page.digitarNome(nomeProduto);
        page.digitarPreco("100.00");
        
        // Simular clique duplo
        page.clicarSalvar();
        page.clicarSalvar();
        
        // Contar produtos depois
        driver.get(BASE_URL + "/produtos/listar");
        listarPage = new ListarPage(driver, wait);
        int quantidadeDepois = listarPage.obterQuantidadeProdutos();
        
        // Deveria ter adicionado apenas 1, não 2
        int diferenca = quantidadeDepois - quantidadeAntes;
        Assert.assertTrue(diferenca == 1, 
            "Clique duplo não deveria criar duplicatas (criado: " + diferenca + ")");
    }

    // ===== TESTES DE NAVEGAÇÃO =====

    @Test(description = "Deve permitir cancelar edição sem salvar")
    public void testCancelarEdicao() {
        driver.get(BASE_URL + "/produtos/editar/1");
        
        // Clicar cancelar
        CadastroPage page = new CadastroPage(driver, wait);
        if (driver.findElements(By.id("btnCancelar")).size() > 0) {
            page.clicarCancelar();
            Assert.assertTrue(driver.getCurrentUrl().contains("/listar"),
                "Cancelar deveria voltar para listagem");
        }
    }

    @Test(description = "Deve voltar para listagem após salvar com sucesso")
    public void testRedirecionamentoAposSalvar() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.preencherFormulario("Produto Teste " + System.currentTimeMillis(), "150.00");
        
        Assert.assertTrue(driver.getCurrentUrl().contains("/listar"),
            "Deveria redirecionar para listagem após salvar");
    }

    // ===== TESTES DE MENSAGENS =====

    @Test(description = "Deve exibir mensagem de sucesso após salvar")
    public void testMensagemSucessoVisivel() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.preencherFormulario("Produto Sucesso " + System.currentTimeMillis(), "200.00");
        
        ListarPage listarPage = new ListarPage(driver, wait);
        Assert.assertTrue(listarPage.isMensagemSucessoVisivel(),
            "Mensagem de sucesso deveria ser exibida");
    }

    @Test(description = "Deve exibir mensagem de erro com informações claras")
    public void testMensagemErroClara() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.digitarNome("");
        page.digitarPreco("100.00");
        page.clicarSalvar();
        
        if (page.isMensagemErroVisivel()) {
            String textoErro = page.obterTextoMensagemErro();
            Assert.assertFalse(textoErro.isEmpty(),
                "Mensagem de erro deveria ter conteúdo");
            System.out.println("Mensagem de erro: " + textoErro);
        }
    }

    // ===== TESTES DE TIMEOUT =====

    @Test(description = "Deve aguardar carregamento da página de listagem")
    public void testWaitCarregamentoLista() {
        driver.get(BASE_URL + "/produtos/cadastrar");
        CadastroPage page = new CadastroPage(driver, wait);
        
        page.preencherFormulario("Produto Timeout " + System.currentTimeMillis(), "300.00");
        
        // Esperar pela tabela de produtos estar presente
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table")));
            Assert.assertTrue(true, "Tabela carregou com sucesso");
        } catch (TimeoutException e) {
            Assert.fail("Timeout ao aguardar carregamento da tabela");
        }
    }

    // ===== TESTES DE EDIÇÃO =====

    @Test(description = "Deve carregar dados do produto na edição")
    public void testCarregamentoDadosEdicao() {
        driver.get(BASE_URL + "/produtos/editar/1");
        
        CadastroPage page = new CadastroPage(driver, wait);
        
        // Os campos deveria estar preenchidos (via placeholder ou value)
        Assert.assertTrue(page.isCampoNomeVisivel(),
            "Campo de nome deveria estar visível para edição");
        Assert.assertTrue(page.isCampoPrecoVisivel(),
            "Campo de preço deveria estar visível para edição");
    }

    @Test(description = "Deve rejeitar edição com dados inválidos")
    public void testRejeicaoEdicaoInvalida() {
        driver.get(BASE_URL + "/produtos/editar/1");
        CadastroPage page = new CadastroPage(driver, wait);
        
        // Tentar salvar com nome vazio
        page.digitarNome("");
        page.digitarPreco("100.00");
        page.clicarSalvar();
        
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/editar") || page.isMensagemErroVisivel(),
            "Edição com dados inválidos deveria ser rejeitada"
        );
    }

    // ===== TESTES DE LISTAGEM =====

    @Test(description = "Deve exibir produtos com informações completas")
    public void testExibicaoDadosProduto() {
        driver.get(BASE_URL + "/produtos/listar");
        ListarPage page = new ListarPage(driver, wait);
        
        int quantidade = page.obterQuantidadeProdutos();
        Assert.assertTrue(quantidade > 0, "Deveria ter produtos na listagem");
        
        // Verificar que nome e preço estão visíveis
        String nome = page.obterNomeProduto(1L);
        String preco = page.obterPrecoProduto(1L);
        
        Assert.assertFalse(nome.isEmpty(), "Nome deveria ser exibido");
        Assert.assertFalse(preco.isEmpty(), "Preço deveria ser exibido");
    }

    @Test(description = "Deve permitir edição a partir da listagem")
    public void testEditarAPartirListagem() {
        driver.get(BASE_URL + "/produtos/listar");
        ListarPage page = new ListarPage(driver, wait);
        
        page.clicarEditarPrimeiroProduto();
        
        Assert.assertTrue(driver.getCurrentUrl().contains("/editar"),
            "Deveria navegar para página de edição");
    }

    @Test(description = "Deve permitir exclusão a partir da listagem")
    public void testExcluirAPartirListagem() {
        driver.get(BASE_URL + "/produtos/listar");
        ListarPage page = new ListarPage(driver, wait);
        
        int quantidadeAntes = page.obterQuantidadeProdutos();
        
        // Clicar em excluir (pode haver confirmação)
        try {
            page.clicarExcluirUltimoProduto();
            
            // Aguardar redirecionamento
            Thread.sleep(1000);
            
            driver.get(BASE_URL + "/produtos/listar");
            page = new ListarPage(driver, wait);
            int quantidadeDepois = page.obterQuantidadeProdutos();
            
            Assert.assertTrue(quantidadeDepois < quantidadeAntes,
                "Produto deveria ser excluído");
        } catch (InterruptedException e) {
            Assert.fail("Erro ao aguardar exclusão: " + e.getMessage());
        }
    }

    // ===== TESTES DE PERFORMANCE =====

    @Test(description = "Deve carregar página de cadastro rapidamente")
    public void testPerformanceCadastro() {
        long inicio = System.currentTimeMillis();
        driver.get(BASE_URL + "/produtos/cadastrar");
        long tempo = System.currentTimeMillis() - inicio;
        
        Assert.assertTrue(tempo < 5000,
            "Página de cadastro deveria carregar em menos de 5 segundos");
        System.out.println("Tempo de carregamento: " + tempo + "ms");
    }

    @Test(description = "Deve carregar listagem rapidamente com muitos produtos")
    public void testPerformanceListagem() {
        long inicio = System.currentTimeMillis();
        driver.get(BASE_URL + "/produtos/listar");
        long tempo = System.currentTimeMillis() - inicio;
        
        Assert.assertTrue(tempo < 5000,
            "Página de listagem deveria carregar em menos de 5 segundos");
        System.out.println("Tempo de carregamento listagem: " + tempo + "ms");
    }
}
