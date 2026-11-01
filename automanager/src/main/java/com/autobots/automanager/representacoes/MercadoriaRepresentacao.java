package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.entidades.Mercadoria;

@Component
public class MercadoriaRepresentacao implements RepresentationModelAssembler<Mercadoria, EntityModel<Mercadoria>> {
    @Override
    public EntityModel<Mercadoria> toModel(Mercadoria mercadoria) {
        long id = mercadoria.getId();
        var controle = methodOn(MercadoriaControle.class);
        var modelo = EntityModel.of(mercadoria,
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarMercadoria(id, null)))
                        .andAffordance(afford(controle.excluirMercadoria(id))),
                linkTo(controle.listar(null, null)).withRel("mercadorias").expand(),
                Ligacoes.empresa(mercadoria.getEmpresa().getId(), "empresa"), Ligacoes.inicio());
        if (mercadoria.getFornecedor() != null) modelo.add(Ligacoes.usuario(mercadoria.getFornecedor().getId(), "fornecedor"));
        return modelo;
    }
}
