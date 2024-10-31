package co.spribe.exchangerate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CurrencyDto(@NotBlank(message = "Currency code cannot be null")
                          @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be exactly 3 uppercase letters")
                          String code) {
}
