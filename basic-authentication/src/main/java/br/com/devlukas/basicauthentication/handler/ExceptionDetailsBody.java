package br.com.devlukas.basicauthentication.handler;

import java.time.LocalDateTime;
import java.util.List;

public record ExceptionDetailsBody(
        String path,
        List<String> messages,
        int statusCode,
        LocalDateTime localDateTime
) {
}
