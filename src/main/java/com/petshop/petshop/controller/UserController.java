package com.petshop.petshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.petshop.petshop.model.User;
import com.petshop.petshop.service.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // Abre a página de cadastro de usuário
    @GetMapping("/register")
    public String register() {
        return "user/registerUser";
    }

    // Lê os dados do formulário e salva o usuário no banco
    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute User user, Model model) {
        Long id = userService.saveUser(user);
        String mensagem = "Usuário '" + id + "' cadastrado com sucesso!";
        model.addAttribute("msg", mensagem);
        return "user/registerUser";
    }

    @GetMapping("/accessDenied")
    public String getAccessDeniedPage() {
        return "user/accessDeniedPage";
    }
}
