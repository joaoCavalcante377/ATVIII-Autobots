package com.autobots.automanager.entidades;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Venda de mercadorias e servicos de uma empresa para um cliente, opcionalmente ligada a um veiculo. */
@Getter
@Setter
@Entity
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Date cadastro;
    @Column(nullable = false, unique = true)
    private String identificacao;
    @ManyToOne(optional = false)
    private Empresa empresa;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Usuario cliente;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Usuario funcionario;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "venda_mercadorias")
    private Set<Mercadoria> mercadorias = new HashSet<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "venda_servicos")
    private Set<Servico> servicos = new HashSet<>();
    @ManyToOne(fetch = FetchType.EAGER)
    private Veiculo veiculo;

    public BigDecimal valorTotal() {
        return mercadorias.stream().map(Mercadoria::getValor).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(servicos.stream().map(Servico::getValor).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
