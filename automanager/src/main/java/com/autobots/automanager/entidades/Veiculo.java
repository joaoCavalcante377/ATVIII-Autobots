package com.autobots.automanager.entidades;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;
import com.autobots.automanager.enumeracoes.TipoVeiculo;

@Getter
@Setter
@Entity
@Relation(itemRelation = "veiculo", collectionRelation = "veiculos")
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVeiculo tipo;
    @Column(nullable = false)
    private String modelo;
    @Column(nullable = false, unique = true)
    private String placa;
    @JsonIgnore
    @ManyToOne(optional = false)
    private Usuario proprietario;
    @JsonIgnore
    @OneToMany(mappedBy = "veiculo")
    private Set<Venda> vendas = new HashSet<>();
}
