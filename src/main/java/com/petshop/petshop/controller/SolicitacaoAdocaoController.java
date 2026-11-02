package com.petshop.petshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.petshop.petshop.model.SolicitacaoAdocao;
import com.petshop.petshop.service.PetService;
import com.petshop.petshop.service.SolicitacaoAdocaoService;

@Controller
@RequestMapping("/adocao")
public class SolicitacaoAdocaoController {

    @Autowired
    private SolicitacaoAdocaoService solicitacaoService;

    @Autowired
    private PetService petService;

    // Lista de solicitações
    @GetMapping
    public String index(Model model) {
        model.addAttribute("solicitacoes", solicitacaoService.getAllSolicitacoes());
        return "adocao/index";
    }

    // Formulário de nova solicitação (a lista de pets alimenta o select de cada item)
    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("solicitacao", new SolicitacaoAdocao());
        model.addAttribute("pets", petService.getAllPets());
        return "adocao/form";
    }

    // Salva nova ou editada; se uma regra de negócio falhar, volta para o form com a mensagem
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("solicitacao") SolicitacaoAdocao solicitacao, BindingResult bindingResult,
            Model model) {
        try {
            solicitacaoService.saveSolicitacao(solicitacao);
        } catch (IllegalStateException e) {
            bindingResult.addError(new ObjectError("solicitacao", e.getMessage()));
            model.addAttribute("pets", petService.getAllPets());
            return "adocao/form";
        }
        return "redirect:/adocao";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        SolicitacaoAdocao solicitacao = solicitacaoService.getSolicitacaoById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        model.addAttribute("solicitacao", solicitacao);
        model.addAttribute("pets", petService.getAllPets());
        return "adocao/form";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        solicitacaoService.deleteSolicitacaoById(id);
        return "redirect:/adocao";
    }
}
