package com.autobots.automanager.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

/** Peca ou produto vendido pela empresa. */
@Getter
@Setter
@Entity
@Relation(itemRelation = "mercadoria", collectionRelation = "mercadorias")
public class Mercadoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private LocalDate validade;
    @Column
    private LocalDate fabricacao;
    @Column(nullable = false)
    private Date cadastro;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private long quantidade;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
    @Column
    private String descricao;
    @JsonIgnore
    @ManyToOne(optional = false)
    private Empresa empresa;
    @JsonIgnore
    @ManyToOne
    private Usuario fornecedor;
}
