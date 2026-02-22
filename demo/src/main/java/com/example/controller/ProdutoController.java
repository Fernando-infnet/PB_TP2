package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.model.Produto;
import com.example.service.ProdutoService;

/**
 * Controlador para gerenciar operações de produtos.
 * Manipula requisições HTTP e respostas para operações CRUD de produtos.
 */
@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    /**
     * Exibe lista de todos os produtos
     */
    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        return "listar";
    }

    /**
     * Exibe formulário para criar novo produto
     */
    @GetMapping("/cadastrar")
    public String cadastrarForm(Model model) {
        model.addAttribute("produto", new Produto());
        return "form";
    }

    /**
     * Salva novo produto ou atualiza existente
     * Implementa tratamento seguro de erros sem exposição de informações internas
     */
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Produto produto, RedirectAttributes redirectAttributes) {
        try {
            // Validação básica antes de processar
            if (produto == null) {
                redirectAttributes.addFlashAttribute("erro", "Dados do produto inválidos. Por favor, preencha todos os campos.");
                return "redirect:/produtos/cadastrar";
            }

            produtoService.salvar(produto);
            redirectAttributes.addFlashAttribute("sucesso", "Produto salvo com sucesso!");
            return "redirect:/produtos/listar";
            
        } catch (IllegalArgumentException e) {
            // Exibe mensagem de validação amigável ao usuário
            String mensagem = normalizarMensagemErro(e.getMessage());
            redirectAttributes.addFlashAttribute("erro", mensagem);
            redirectAttributes.addFlashAttribute("produto", produto);
            return "redirect:/produtos/cadastrar";
            
        } catch (Exception e) {
            // Erro inesperado - mensagem genérica para segurança
            redirectAttributes.addFlashAttribute("erro", "Erro ao processar a solicitação. Por favor, tente novamente.");
            redirectAttributes.addFlashAttribute("produto", produto);
            return "redirect:/produtos/cadastrar";
        }
    }

    /**
     * Exibe formulário para editar produto existente
     */
    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id <= 0) {
                redirectAttributes.addFlashAttribute("erro", "ID do produto inválido.");
                return "redirect:/produtos/listar";
            }

            var produto = produtoService.buscarPorId(id);
            if (produto.isPresent()) {
                model.addAttribute("produto", produto.get());
                return "form";
            }
            redirectAttributes.addFlashAttribute("erro", "Produto não encontrado. Ele pode ter sido removido.");
            return "redirect:/produtos/listar";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao carregar o produto. Tente novamente.");
            return "redirect:/produtos/listar";
        }
    }

    /**
     * Deleta um produto por ID com validação segura
     */
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id <= 0) {
                redirectAttributes.addFlashAttribute("erro", "ID do produto inválido.");
                return "redirect:/produtos/listar";
            }

            if (produtoService.excluir(id)) {
                redirectAttributes.addFlashAttribute("sucesso", "Produto removido do estoque com sucesso!");
            } else {
                redirectAttributes.addFlashAttribute("erro", "Produto não encontrado. Ele pode ter sido removido anteriormente.");
            }
            return "redirect:/produtos/listar";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover o produto. Tente novamente.");
            return "redirect:/produtos/listar";
        }
    }

    /**
     * Normaliza mensagem de erro para ser amigável ao usuário
     * Remove detalhes técnicos e expõe apenas informações relevantes
     */
    private String normalizarMensagemErro(String mensagemOriginal) {
        if (mensagemOriginal == null || mensagemOriginal.trim().isEmpty()) {
            return "Dados inválidos. Por favor, verifique e tente novamente.";
        }

        // Mapeamento de mensagens técnicas para mensagens amigáveis
        String mensagem = mensagemOriginal.toLowerCase();

        if (mensagem.contains("nome") && (mensagem.contains("vazio") || mensagem.contains("espaço"))) {
            return "Nome do produto é obrigatório. Por favor, insira um nome válido.";
        }

        if (mensagem.contains("preço") && mensagem.contains("negativo")) {
            return "Preço deve ser um valor positivo. Por favor, insira um preço maior que zero.";
        }

        if (mensagem.contains("preço") && mensagem.contains("zero")) {
            return "Preço deve ser maior que zero.";
        }

        if (mensagem.contains("preço") && (mensagem.contains("null") || mensagem.contains("nulo"))) {
            return "Preço é obrigatório. Por favor, insira um valor.";
        }

        if (mensagem.contains("nome") && (mensagem.contains("null") || mensagem.contains("nulo"))) {
            return "Nome do produto é obrigatório.";
        }

        if (mensagem.contains("tamanho") || mensagem.contains("exceder") || mensagem.contains("limite")) {
            return "Nome do produto muito longo. Use no máximo 255 caracteres.";
        }

        // Fallback seguro para erros desconhecidos
        return "Dados inválidos. Por favor, verifique e tente novamente.";
    }
}
