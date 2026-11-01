package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoRequisicao {
    private @NotBlank String estado;
    private @NotBlank String cidade;
    private @NotBlank String bairro;
    private @NotBlank String rua;
    private @NotBlank String numero;
    private @NotBlank String codigoPostal;
    private String informacoesAdicionais;
}

