package br.com.devlukas.basicauthentication.security;

import br.com.devlukas.basicauthentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .build();
    }
}
