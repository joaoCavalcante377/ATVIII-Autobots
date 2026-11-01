package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Cadastro de empresa; endereco e telefones podem ser enviados junto. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaRequisicao {
    private @NotBlank String razaoSocial;
    private String nomeFantasia;
    private @Valid EnderecoRequisicao endereco;
    private List<@NotNull @Valid TelefoneRequisicao> telefones;
}

