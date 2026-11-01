package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.autobots.automanager.enumeracoes.TipoVeiculo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoRequisicao {
    private @NotNull TipoVeiculo tipo;
    private @NotBlank String modelo;
    private @NotBlank @Pattern(regexp = "(?i)\\s*[A-Z]{3}-?\\d[A-Z0-9]\\d{2}\\s*", message = "deve seguir o formato ABC-1234 ou ABC1D23") String placa;
}

