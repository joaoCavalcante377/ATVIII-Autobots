package com.autobots.automanager.controles;

import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import com.autobots.automanager.representacoes.Ligacoes;

/** Monta colecoes HAL com link proprio, link para o inicio e o nome correto mesmo quando a lista esta vazia. */
final class Colecoes {
    private Colecoes() {
    }

    static <E, R> CollectionModel<EntityModel<R>> de(List<E> registros, RepresentationModelAssembler<E, EntityModel<R>> montador,
            Class<?> tipo, Link proprio, Link... extras) {
        var modelo = CollectionModel.of(registros.stream().map(montador::toModel).toList(), proprio, Ligacoes.inicio())
                .withFallbackType(EntityModel.class, tipo);
        modelo.add(extras);
        return modelo;
    }
}
