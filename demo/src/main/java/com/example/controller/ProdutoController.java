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
     */
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Produto produto, RedirectAttributes redirectAttributes) {
        try {
            produtoService.salvar(produto);
            redirectAttributes.addFlashAttribute("sucesso", "Produto salvo com sucesso!");
            return "redirect:/produtos/listar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/produtos/cadastrar";
        }
    }

    /**
     * Exibe formulário para editar produto existente
     */
    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var produto = produtoService.buscarPorId(id);
        if (produto.isPresent()) {
            model.addAttribute("produto", produto.get());
            return "form";
        }
        redirectAttributes.addFlashAttribute("erro", "Produto não encontrado");
        return "redirect:/produtos/listar";
    }

    /**
     * Deleta um produto por ID
     */
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (produtoService.excluir(id)) {
            redirectAttributes.addFlashAttribute("sucesso", "Produto deletado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Produto não encontrado");
        }
        return "redirect:/produtos/listar";
    }
}
