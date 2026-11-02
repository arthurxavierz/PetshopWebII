package com.petshop.petshop.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.model.SolicitacaoAdocao;
import com.petshop.petshop.service.SolicitacaoAdocaoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/adocao")
public class SolicitacaoAdocaoController {

    @Autowired
    private SolicitacaoAdocaoService solicitacaoService;

    // O Admin vê todas; o usuário padrão vê só as dele ("Minhas solicitações")
    @GetMapping
    public String index(Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("Admin"));

        if (isAdmin) {
            model.addAttribute("solicitacoes", solicitacaoService.getAllSolicitacoes());
        } else {
            model.addAttribute("solicitacoes", solicitacaoService.getSolicitacoesDoUsuario(authentication.getName()));
        }
        return "adocao/index";
    }

    // Formulário do usuário padrão; petId vem do botão "Solicitar adoção" da lista de pets
    @GetMapping("/nova")
    public String nova(@RequestParam(required = false) Long petId, Model model) {
        SolicitacaoAdocao solicitacao = new SolicitacaoAdocao();
        Pet pet = new Pet();
        pet.setId(petId);
        solicitacao.setPet(pet);

        model.addAttribute("solicitacao", solicitacao);
        model.addAttribute("pets", solicitacaoService.getPetsDisponiveis());
        return "adocao/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("solicitacao") SolicitacaoAdocao solicitacao, BindingResult result,
            Principal principal, Model model) {

        // Quem enviou é o usuário logado (não vem do formulário)
        solicitacao.setEmailUsuario(principal.getName());

        if (!result.hasErrors()) {
            try {
                solicitacaoService.enviarSolicitacao(solicitacao);
                return "redirect:/adocao";
            } catch (IllegalStateException e) {
                result.addError(new ObjectError("solicitacao", e.getMessage()));
            }
        }

        model.addAttribute("pets", solicitacaoService.getPetsDisponiveis());
        return "adocao/form";
    }

    // Só o Admin chega aqui (regra no SecurityConfig)
    @GetMapping("/aprovar/{id}")
    public String aprovar(@PathVariable Long id) {
        try {
            solicitacaoService.aprovarSolicitacao(id);
        } catch (IllegalStateException e) {
            return "redirect:/adocao?erroAprovacao";
        }
        return "redirect:/adocao";
    }
}
