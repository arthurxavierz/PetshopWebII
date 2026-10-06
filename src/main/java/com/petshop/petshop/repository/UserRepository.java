package com.petshop.petshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petshop.petshop.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);
}
