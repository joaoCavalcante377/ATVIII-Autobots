package com.autobots.automanager.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Credencial por cracha ou cartao com codigo de barras. */
@Getter
@Setter
@Entity
public class CredencialCodigoBarra extends Credencial {
    @Column(nullable = false, unique = true)
    private long codigo;
}
