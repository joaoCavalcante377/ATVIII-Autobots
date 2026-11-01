package com.autobots.automanager.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Credencial por nome de usuario e senha. A senha e guardada somente como hash BCrypt. */
@Getter
@Setter
@Entity
public class CredencialUsuarioSenha extends Credencial {
    @Column(nullable = false, unique = true)
    private String nomeUsuario;
    @Column(nullable = false)
    private String senha;
}
