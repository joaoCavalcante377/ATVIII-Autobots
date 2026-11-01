package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicoRequisicao {
    private @NotBlank String nome;
    private String descricao;
    private @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal valor;
}

