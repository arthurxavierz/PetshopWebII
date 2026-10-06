package com.petshop.petshop.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.petshop.petshop.model.User;
import com.petshop.petshop.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long saveUser(User user) {
        String senhaCriptografada = passwordEncoder.encode(user.getPassword());
        user.setPassword(senhaCriptografada);
        user = userRepository.save(user);
        return user.getId();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<User> opt = userRepository.findUserByEmail(email);

        if (opt.isEmpty()) {
            throw new UsernameNotFoundException("Usuário com email: " + email + " não encontrado");
        }

        User user = opt.get();
        List<String> roles = user.getRoles();
        Set<GrantedAuthority> autoridades = new HashSet<>();
        if (roles != null) {
            for (String role : roles) {
                autoridades.add(new SimpleGrantedAuthority(role));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                email,
                user.getPassword(),
                autoridades);
    }
}
