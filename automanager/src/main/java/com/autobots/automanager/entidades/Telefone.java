package com.autobots.automanager.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@Entity
@Relation(itemRelation = "telefone", collectionRelation = "telefones")
public class Telefone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String ddd;
    @Column(nullable = false)
    private String numero;
}
