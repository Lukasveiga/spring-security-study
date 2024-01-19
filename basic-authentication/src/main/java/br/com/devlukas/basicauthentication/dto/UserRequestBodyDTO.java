package br.com.devlukas.basicauthentication.dto;

import br.com.devlukas.basicauthentication.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserRequestBodyDTO(

        @Email(message = "Provide a valid email")
        @NotBlank(message = "Username cannot be blank")
        String username,

        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\-]).{8,}$",
        message = "Password requires at least 8 characters, with numbers, upper and lower case letters and special characters")
        String password) {

    public User toEntity() {
        return new User(this.username, this.password);
    }
}
