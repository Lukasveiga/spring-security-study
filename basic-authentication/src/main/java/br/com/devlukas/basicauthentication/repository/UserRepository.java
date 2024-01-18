package br.com.devlukas.basicauthentication.repository;

import br.com.devlukas.basicauthentication.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
