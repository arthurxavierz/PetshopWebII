package com.petshop.petshop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.petshop.petshop.model.Pet;
import com.petshop.petshop.repository.ItemSolicitacaoRepository;
import com.petshop.petshop.repository.PetRepository;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final ItemSolicitacaoRepository itemSolicitacaoRepository;

    public PetServiceImpl(PetRepository petRepository, ItemSolicitacaoRepository itemSolicitacaoRepository) {
        this.petRepository = petRepository;
        this.itemSolicitacaoRepository = itemSolicitacaoRepository;
    }

    @Override
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    @Override
    public Pet savePet(Pet pet) {
        return petRepository.save(pet);
    }

    @Override
    public Pet getPetById(Long id) {
        return petRepository.findById(id).orElse(null);
    }

    @Override
    public Pet updatePet(Long id, Pet petAtualizado) {
        Pet pet = petRepository.findById(id).orElseThrow(() -> new RuntimeException("Pet não encontrado"));

        pet.setNome(petAtualizado.getNome());
        pet.setEspecie(petAtualizado.getEspecie());
        pet.setRaca(petAtualizado.getRaca());
        pet.setIdade(petAtualizado.getIdade());
        pet.setTutor(petAtualizado.getTutor());

        return petRepository.save(pet);
    }

    @Override
    public void deletePet(Long id) {
        // Um pet que está em alguma solicitação de adoção não pode ser excluído
        if (itemSolicitacaoRepository.existsByPetId(id)) {
            throw new IllegalStateException("Este pet está em uma solicitação de adoção e não pode ser excluído.");
        }
        petRepository.deleteById(id);
    }
}