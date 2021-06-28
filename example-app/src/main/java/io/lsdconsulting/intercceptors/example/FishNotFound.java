package io.lsdconsulting.intercceptors.example;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "fish not found")
public class FishNotFound extends RuntimeException {
}
