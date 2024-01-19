package br.com.devlukas.basicauthentication.controller;

import br.com.devlukas.basicauthentication.dto.UserRequestBodyDTO;
import br.com.devlukas.basicauthentication.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/basic-authn")
public class BasicAuthnController {

    private final UserService userService;

    public BasicAuthnController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/singup")
    public ResponseEntity<String> singUp(@RequestBody @Valid UserRequestBodyDTO userRequestBodyDTO) {
        userService.registerUser(userRequestBodyDTO.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User %s successfully registered!".formatted(userRequestBodyDTO.username()));
    }

    @GetMapping
    public ResponseEntity<String> onlyAuthenticated() {
        return ResponseEntity.ok("Private message only for athenticated users.");
    }
}
