package com.petshop.petshop.service;

import java.util.List;

import com.petshop.petshop.model.Pet;

public interface PetService {

    List<Pet> getAllPets();

    Pet savePet(Pet pet);

    Pet getPetById(Long id);

    Pet updatePet(Long id, Pet pet);

    void deletePet(Long id);

}