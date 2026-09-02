package com.petshop.petshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.petshop.petshop.model.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {
}