package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAtualizacao {
    private @NotBlank String nome;
    private String nomeSocial;
    private @NotEmpty Set<@NotNull PerfilUsuario> perfis;
}

