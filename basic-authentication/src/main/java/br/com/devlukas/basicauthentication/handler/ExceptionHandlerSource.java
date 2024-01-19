package br.com.devlukas.basicauthentication.handler;

import br.com.devlukas.basicauthentication.service.exceptions.UserAlreadyRegisteredException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionHandlerSource extends ResponseEntityExceptionHandler {

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class, InsufficientAuthenticationException.class})
    public ResponseEntity<ExceptionDetailsBody> handleAuthenticationException(Exception ex, HttpServletRequest request) {

        String message = ex.getMessage();
        HttpStatus statusCode = HttpStatus.FORBIDDEN;

        if(!(ex instanceof InsufficientAuthenticationException)) {
            message = "Email or password is incorrect";
            statusCode = HttpStatus.UNAUTHORIZED;
        }

        return new ResponseEntity<>(new ExceptionDetailsBody(
                request.getRequestURI(),
                List.of(message),
                statusCode.value(),
                LocalDateTime.now()
        ), statusCode);
    }

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ExceptionDetailsBody> userAlredyRegistered(UserAlreadyRegisteredException ex,
                                                                     HttpServletRequest request) {

        return new ResponseEntity<>(new ExceptionDetailsBody(
                request.getRequestURI(),
                List.of(ex.getMessage()),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        ), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        var fieldsMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());

        return new ResponseEntity<>(new ExceptionDetailsBody(
                ((ServletWebRequest)request).getRequest().getRequestURI(),
                fieldsMessage,
                status.value(),
                LocalDateTime.now()
        ), HttpStatus.BAD_REQUEST);
    }
}
