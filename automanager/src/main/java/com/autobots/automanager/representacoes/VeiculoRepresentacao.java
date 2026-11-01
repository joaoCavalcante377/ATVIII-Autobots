package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.*;
import com.autobots.automanager.entidades.Veiculo;

@Component
public class VeiculoRepresentacao implements RepresentationModelAssembler<Veiculo, EntityModel<Veiculo>> {
    @Override
    public EntityModel<Veiculo> toModel(Veiculo veiculo) {
        long id = veiculo.getId();
        var controle = methodOn(VeiculoControle.class);
        return EntityModel.of(veiculo,
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarVeiculo(id, null)))
                        .andAffordance(afford(controle.excluirVeiculo(id))),
                linkTo(controle.listar(null)).withRel("veiculos").expand(),
                Ligacoes.usuario(veiculo.getProprietario().getId(), "proprietario"),
                linkTo(methodOn(VendaControle.class).listar(null, null, null, id)).withRel("vendas").expand(),
                Ligacoes.inicio());
    }
}
