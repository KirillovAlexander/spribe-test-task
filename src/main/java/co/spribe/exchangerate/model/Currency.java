package co.spribe.exchangerate.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("currency")
public record Currency(@Id UUID id, String code) {
    public Currency(final String code) {
        this(null, code);
    }
}