package br.com.devlukas.basicauthentication.controller;

import br.com.devlukas.basicauthentication.domain.User;
import br.com.devlukas.basicauthentication.dto.UserRequestBodyDTO;
import br.com.devlukas.basicauthentication.handler.ExceptionDetailsBody;
import br.com.devlukas.basicauthentication.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BasicAuthnControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    private static final String BASE_URL = "/api/v1/basic-authn";

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void singUp_shouldReturnSuccessfulRegistrationMessage_whenValidUserParamsAreProvided() {
        var validUser = new UserRequestBodyDTO("valid_user_email@email.com", "Valid_password_1*");
        ResponseEntity<String> exchange = testRestTemplate
                .postForEntity(BASE_URL + "/singup", validUser, String.class);

        var responseBody = exchange.getBody();

        Assertions.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseBody).isEqualTo("User %s successfully registered!".formatted(validUser.username()));
    }

    @Test
    public void singUp_shouldReturnStatusCode400_whenEmptyUsernameIsProvided() throws JsonProcessingException {
        var validUser = new UserRequestBodyDTO("", "Valid_password_1*");
        ResponseEntity<String> exchange = testRestTemplate
                .postForEntity(BASE_URL + "/singup", validUser, String.class);

        var responseBody = exchange.getBody();
        var exceptionBody = objectMapper.readValue(responseBody, ExceptionDetailsBody.class);

        Assertions.assertThat(exceptionBody.path()).isEqualTo(BASE_URL + "/singup");
        Assertions.assertThat(exceptionBody.messages()).isEqualTo(List.of("Username cannot be blank"));
        Assertions.assertThat(exceptionBody.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(exceptionBody.localDateTime()).isNotNull();
    }

    @Test
    public void singUp_shouldReturnStatusCode400_whenInvalidUsernameIsProvided() throws JsonProcessingException {
        var validUser = new UserRequestBodyDTO("invalid_email", "Valid_password_1*");
        ResponseEntity<String> exchange = testRestTemplate
                .postForEntity(BASE_URL + "/singup", validUser, String.class);

        var responseBody = exchange.getBody();
        var exceptionBody = objectMapper.readValue(responseBody, ExceptionDetailsBody.class);

        Assertions.assertThat(exceptionBody.path()).isEqualTo(BASE_URL + "/singup");
        Assertions.assertThat(exceptionBody.messages()).isEqualTo(List.of("Provide a valid email"));
        Assertions.assertThat(exceptionBody.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(exceptionBody.localDateTime()).isNotNull();
    }

    @Test
    public void singUp_shouldReturnStatusCode400_whenEmptyPasswordIsProvided() throws JsonProcessingException {
        var validUser = new UserRequestBodyDTO("valid_user_email@email.com", "");
        ResponseEntity<String> exchange = testRestTemplate
                .postForEntity(BASE_URL + "/singup", validUser, String.class);

        var responseBody = exchange.getBody();
        var exceptionBody = objectMapper.readValue(responseBody, ExceptionDetailsBody.class);

        Assertions.assertThat(exceptionBody.path()).isEqualTo(BASE_URL + "/singup");
        Assertions.assertThat(exceptionBody.messages())
                .isEqualTo(List.of("Password requires at least 8 characters, with numbers, upper and lower case letters and special characters"));
        Assertions.assertThat(exceptionBody.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(exceptionBody.localDateTime()).isNotNull();
    }

    @Test
    public void singUp_shouldReturnStatusCode400_whenInvalidPasswordIsProvided() throws JsonProcessingException {
        var validUser = new UserRequestBodyDTO("valid_user_email@email.com", "invalid_password");
        ResponseEntity<String> exchange = testRestTemplate
                .postForEntity(BASE_URL + "/singup", validUser, String.class);

        var responseBody = exchange.getBody();
        var exceptionBody = objectMapper.readValue(responseBody, ExceptionDetailsBody.class);

        Assertions.assertThat(exceptionBody.path()).isEqualTo(BASE_URL + "/singup");
        Assertions.assertThat(exceptionBody.messages())
                .isEqualTo(List.of("Password requires at least 8 characters, with numbers, upper and lower case letters and special characters"));
        Assertions.assertThat(exceptionBody.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(exceptionBody.localDateTime()).isNotNull();
    }

    @Test
    public void singUp_shouldReturnStatusCode400_whenInvalidParamsAreProvided() throws JsonProcessingException {
        var validUser = new UserRequestBodyDTO("invalid_email", "invalid_password");
        ResponseEntity<String> exchange = testRestTemplate
                .postForEntity(BASE_URL + "/singup", validUser, String.class);

        var responseBody = exchange.getBody();
        var exceptionBody = objectMapper.readValue(responseBody, ExceptionDetailsBody.class);

        Assertions.assertThat(exceptionBody.path()).isEqualTo(BASE_URL + "/singup");
        Assertions.assertThat(exceptionBody.messages())
                .contains("Password requires at least 8 characters, with numbers, upper and lower case letters and special characters",
                        "Provide a valid email");
        Assertions.assertThat(exceptionBody.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(exceptionBody.localDateTime()).isNotNull();
    }

    @Test
    public void onlyAuthenticated_shouldReturnPrivateMessage_whenUserIsSuccessfullyAuthenticated() {
        var username = "valid_user_email@email.com";
        var password = "Valid_password_1*";

        userRepository.save(new User(username, passwordEncoder.encode(password)));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        var userRequest = new UserRequestBodyDTO(username, password);
        HttpEntity<UserRequestBodyDTO> requestEntity = new HttpEntity<>(userRequest, requestHeaders);

        ResponseEntity<String> exchange = testRestTemplate.withBasicAuth(username, password)
                .exchange(BASE_URL, HttpMethod.GET, requestEntity, String.class);

        var responseBody = exchange.getBody();

        Assertions.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseBody).isEqualTo("Private message only for athenticated users.");
    }

    @Test
    public void onlyAuthenticated_shouldReturnStatusCode403_whenUserCredentialsAreNotProvided() throws JsonProcessingException {
        ResponseEntity<String> exchange = testRestTemplate.exchange(BASE_URL, HttpMethod.GET, null, String.class);

        var responseBody = exchange.getBody();
        var exceptionBody = objectMapper.readValue(responseBody, ExceptionDetailsBody.class);

        Assertions.assertThat(exceptionBody.path()).isEqualTo(BASE_URL);
        Assertions.assertThat(exceptionBody.messages())
                .contains("Full authentication is required to access this resource");
        Assertions.assertThat(exceptionBody.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        Assertions.assertThat(exceptionBody.localDateTime()).isNotNull();
    }

    @Test
    public void onlyAuthenticated_shouldReturnStatusCode401_whenInvalidUsernameIsProvided() throws JsonProcessingException {
        var username = "valid_user_email@email.com";
        var password = "Valid_password_1*";

        userRepository.save(new User(username, passwordEncoder.encode(password)));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        var invalidUsername = "invalid_username";

        var userRequest = new UserRequestBodyDTO(invalidUsername, password);
        HttpEntity<UserRequestBodyDTO> requestEntity = new HttpEntity<>(userRequest, requestHeaders);

        ResponseEntity<String> exchange = testRestTemplate.withBasicAuth(invalidUsername, password)
                .exchange(BASE_URL, HttpMethod.GET, requestEntity, String.class);

        var responseBody = exchange.getBody();
        var exceptionBody = objectMapper.readValue(responseBody, ExceptionDetailsBody.class);

        Assertions.assertThat(exceptionBody.path()).isEqualTo(BASE_URL);
        Assertions.assertThat(exceptionBody.messages())
                .contains("Email or password is incorrect");
        Assertions.assertThat(exceptionBody.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        Assertions.assertThat(exceptionBody.localDateTime()).isNotNull();
    }

    @Test
    public void onlyAuthenticated_shouldReturnStatusCode401_whenInvalidPasswordIsProvided() throws JsonProcessingException {
        var username = "valid_user_email@email.com";
        var password = "Valid_password_1*";

        userRepository.save(new User(username, passwordEncoder.encode(password)));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        var invalidPassword = "invalid_password";

        var userRequest = new UserRequestBodyDTO(username, invalidPassword);
        HttpEntity<UserRequestBodyDTO> requestEntity = new HttpEntity<>(userRequest, requestHeaders);

        ResponseEntity<String> exchange = testRestTemplate.withBasicAuth(username, invalidPassword)
                .exchange(BASE_URL, HttpMethod.GET, requestEntity, String.class);

        var responseBody = exchange.getBody();
        var exceptionBody = objectMapper.readValue(responseBody, ExceptionDetailsBody.class);

        Assertions.assertThat(exceptionBody.path()).isEqualTo(BASE_URL);
        Assertions.assertThat(exceptionBody.messages())
                .contains("Email or password is incorrect");
        Assertions.assertThat(exceptionBody.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        Assertions.assertThat(exceptionBody.localDateTime()).isNotNull();
    }

}