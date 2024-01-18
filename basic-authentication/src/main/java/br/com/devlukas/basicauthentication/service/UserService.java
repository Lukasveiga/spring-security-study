package br.com.devlukas.basicauthentication.service;

import br.com.devlukas.basicauthentication.domain.User;
import br.com.devlukas.basicauthentication.repository.UserRepository;
import br.com.devlukas.basicauthentication.service.exceptions.UserAlreadyRegisteredException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) {
        var existingUser = repository.findByEmail(user.getEmail());

        if(existingUser.isPresent())
            throw new UserAlreadyRegisteredException("%s already registered.".formatted(user.getEmail()));

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        repository.save(user);
    }

}
