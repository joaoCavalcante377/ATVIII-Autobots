package com.autobots.automanager.entidades;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

/** Pessoa ou empresa que se relaciona com a loja: cliente, funcionario ou fornecedor. */
@Getter
@Setter
@Entity
@Relation(itemRelation = "usuario", collectionRelation = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column
    private String nomeSocial;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_perfis")
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false)
    private Set<PerfilUsuario> perfis = new HashSet<>();
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_telefones")
    private Set<Telefone> telefones = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Endereco endereco;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_documentos")
    private Set<Documento> documentos = new HashSet<>();
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_emails")
    private Set<Email> emails = new HashSet<>();
    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinTable(name = "usuario_credenciais")
    private Set<Credencial> credenciais = new HashSet<>();
    /** Mercadorias fornecidas por este usuario, quando ele e fornecedor. */
    @JsonIgnore
    @OneToMany(mappedBy = "fornecedor")
    private Set<Mercadoria> mercadorias = new HashSet<>();
    /** Compras deste usuario, quando ele e cliente. */
    @JsonIgnore
    @OneToMany(mappedBy = "cliente")
    private Set<Venda> vendas = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "proprietario")
    private Set<Veiculo> veiculos = new HashSet<>();
}
