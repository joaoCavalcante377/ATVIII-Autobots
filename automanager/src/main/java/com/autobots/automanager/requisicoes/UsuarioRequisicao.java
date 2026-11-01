package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

/** Cadastro de usuario; endereco, telefones, documentos e e-mails podem ser enviados junto. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequisicao {
    private @NotBlank String nome;
    private String nomeSocial;
    private @NotEmpty Set<@NotNull PerfilUsuario> perfis;
    private @Valid EnderecoRequisicao endereco;
    private List<@NotNull @Valid TelefoneRequisicao> telefones;
    private List<@NotNull @Valid DocumentoRequisicao> documentos;
    private List<@NotNull @Valid EmailRequisicao> emails;
}

