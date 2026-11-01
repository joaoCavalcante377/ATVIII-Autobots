package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.servicos.TelefoneServico;

@Component
public class TelefoneRepresentacao implements RepresentationModelAssembler<Telefone, EntityModel<Telefone>> {
    private final TelefoneServico servico;

    public TelefoneRepresentacao(TelefoneServico servico) {
        this.servico = servico;
    }

    @Override
    public EntityModel<Telefone> toModel(Telefone telefone) {
        long id = telefone.getId();
        var controle = methodOn(TelefoneControle.class);
        var modelo = EntityModel.of(telefone,
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarTelefone(id, null)))
                        .andAffordance(afford(controle.excluirTelefone(id))),
                linkTo(controle.listar(null, null)).withRel("telefones").expand(), Ligacoes.inicio());
        servico.usuarioDe(id).ifPresent(dono -> modelo.add(Ligacoes.usuario(dono, "usuario")));
        servico.empresaDe(id).ifPresent(dono -> modelo.add(Ligacoes.empresa(dono, "empresa")));
        return modelo;
    }
}
