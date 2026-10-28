package com.petshop.petshop.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.service.PetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pets")
public class PetRestController {

    @Autowired
    private PetService petService;

    /**
     * Resposta: 200 OK com a lista de pets.
     */
    @GetMapping
    public ResponseEntity<List<Pet>> getAllPets() {
        List<Pet> pets = petService.getAllPets();
        return ResponseEntity.ok(pets);
    }

    /**
     * Resposta: 200 OK com o pet, ou 404 Not Found se não existir.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id) {
        Pet pet = petService.getPetById(id);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pet);
    }

    /**
     * Corpo da Requisição: JSON do pet a ser criado.
     * Resposta: 201 Created com o local do novo recurso e o pet criado.
     * 400 Bad Request se a validação falhar.
     */
    @PostMapping
    public ResponseEntity<?> createPet(@Valid @RequestBody Pet pet, BindingResult result) {
        if (result.hasErrors()) {
            // Retorna 400 Bad Request com os erros de validação
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        Pet petSalvo = petService.savePet(pet);

        // Cria a URI para o novo recurso (ex: /api/pets/5)
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(petSalvo.getId())
                .toUri();

        // Retorna 201 Created
        return ResponseEntity.created(location).body(petSalvo);
    }

    /**
     * Corpo da Requisição: JSON do pet com as atualizações.
     * Resposta: 200 OK com o pet atualizado.
     * 404 Not Found se o pet não existir.
     * 400 Bad Request se a validação falhar.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(@PathVariable Long id, @Valid @RequestBody Pet petDetails, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        try {
            Pet petAtualizado = petService.updatePet(id, petDetails);
            return ResponseEntity.ok(petAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Resposta: 204 No Content se a exclusão for bem-sucedida.
     * 404 Not Found se o pet não existir.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        if (petService.getPetById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        petService.deletePet(id);

        // Retorna 204 No Content (sucesso, sem corpo de resposta)
        return ResponseEntity.noContent().build();
    }
}
