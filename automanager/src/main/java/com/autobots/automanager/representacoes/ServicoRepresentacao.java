package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.entidades.Servico;

@Component
public class ServicoRepresentacao implements RepresentationModelAssembler<Servico, EntityModel<Servico>> {
    @Override
    public EntityModel<Servico> toModel(Servico servico) {
        long id = servico.getId();
        var controle = methodOn(ServicoControle.class);
        return EntityModel.of(servico,
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarServico(id, null)))
                        .andAffordance(afford(controle.excluirServico(id))),
                linkTo(controle.listar(null)).withRel("servicos").expand(),
                Ligacoes.empresa(servico.getEmpresa().getId(), "empresa"), Ligacoes.inicio());
    }
}
