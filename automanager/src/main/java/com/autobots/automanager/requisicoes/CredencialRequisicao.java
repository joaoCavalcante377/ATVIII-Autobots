package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.autobots.automanager.enumeracoes.TipoCredencial;

/**
 * Credencial de acesso. USUARIO_SENHA usa nomeUsuario e senha; CODIGO_BARRA usa codigo.
 * Na atualizacao, a senha pode ser omitida para manter a atual.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CredencialRequisicao {
    private @NotNull TipoCredencial tipo;
    private @Size(min = 3, max = 60) String nomeUsuario;
    private @Size(min = 6, max = 72) String senha;
    private @Positive Long codigo;
    private Boolean inativo;
}

