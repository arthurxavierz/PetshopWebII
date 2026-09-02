package com.petshop.petshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.petshop.petshop.service.PetService;

@Controller
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping("/pet")
    public String index(Model model) {

        model.addAttribute("pets", petService.getAllPets());

        return "pet/index";
    }
}