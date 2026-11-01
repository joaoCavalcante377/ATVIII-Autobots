package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelefoneRequisicao {
    private @NotBlank @Pattern(regexp = "\\d{2,3}", message = "deve ter 2 ou 3 digitos") String ddd;
    private @NotBlank @Pattern(regexp = "\\d{8,10}", message = "deve ter de 8 a 10 digitos") String numero;
}

