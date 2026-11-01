package com.autobots.automanager.entidades;

import java.util.Date;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Forma de acesso de um usuario ao sistema. As subclasses definem o meio de identificacao. */
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Credencial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Date criacao;
    @Column
    private Date ultimoAcesso;
    @Column(nullable = false)
    private boolean inativo;
}
