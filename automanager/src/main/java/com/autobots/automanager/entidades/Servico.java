package com.autobots.automanager.entidades;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

/** Servico de manutencao oferecido pela empresa. */
@Getter
@Setter
@Entity
@Relation(itemRelation = "servico", collectionRelation = "servicos")
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
    @Column
    private String descricao;
    @JsonIgnore
    @ManyToOne(optional = false)
    private Empresa empresa;
}
