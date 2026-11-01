package com.autobots.automanager.entidades;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;
import com.autobots.automanager.enumeracoes.TipoDocumento;

@Getter
@Setter
@Entity
@Relation(itemRelation = "documento", collectionRelation = "documentos")
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipo;
    @Column(nullable = false)
    private LocalDate dataEmissao;
    @Column(nullable = false, unique = true)
    private String numero;
}
