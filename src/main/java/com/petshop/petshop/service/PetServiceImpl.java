package com.petshop.petshop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.repository.PetRepository;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;

    public PetServiceImpl(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }
}