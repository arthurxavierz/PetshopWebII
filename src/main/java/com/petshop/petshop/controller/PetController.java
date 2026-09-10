package com.petshop.petshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.petshop.petshop.model.Pet;
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

    @GetMapping("/pet/novo")
    public String novo(Model model) {
        model.addAttribute("pet", new Pet());
        return "pet/form";
    }

    @PostMapping("/pet/salvar")
    public String salvar(@ModelAttribute Pet pet) {
        petService.savePet(pet);
        return "redirect:/pet";
    }

    @GetMapping("/pet/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Pet pet = petService.getPetById(id);

        if (pet == null) {
            return "redirect:/pet";
        }

        model.addAttribute("pet", pet);
        return "pet/form";
    }

    @PostMapping("/pet/atualizar/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute Pet pet) {
        petService.updatePet(id, pet);
        return "redirect:/pet";
    }

    @GetMapping("/pet/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        petService.deletePet(id);
        return "redirect:/pet";
    }
}