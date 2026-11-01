package com.autobots.automanager.entidades;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

/** Unidade comercial que presta servicos de manutencao e vende mercadorias. */
@Getter
@Setter
@Entity
@Relation(itemRelation = "empresa", collectionRelation = "empresas")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String razaoSocial;
    @Column
    private String nomeFantasia;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "empresa_telefones")
    private Set<Telefone> telefones = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Endereco endereco;
    @Column(nullable = false)
    private Date cadastro;
    /** Um usuario pode estar associado a mais de uma empresa, por exemplo um cliente das duas redes. */
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "empresa_usuarios")
    private Set<Usuario> usuarios = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "empresa")
    private Set<Mercadoria> mercadorias = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "empresa")
    private Set<Servico> servicos = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "empresa")
    private Set<Venda> vendas = new HashSet<>();
}
