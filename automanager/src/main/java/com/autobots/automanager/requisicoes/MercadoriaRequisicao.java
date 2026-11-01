package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Mercadoria da empresa; fornecedorId e opcional e deve ser um fornecedor associado a empresa. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoriaRequisicao {
    private @NotBlank String nome;
    private String descricao;
    private @NotNull @PositiveOrZero Long quantidade;
    private @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal valor;
    private LocalDate fabricacao;
    private LocalDate validade;
    private Long fornecedorId;
}

