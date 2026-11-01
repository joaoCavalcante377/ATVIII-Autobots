package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.CredencialControle;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.servicos.CredencialServico;

@Component
public class CredencialRepresentacao implements RepresentationModelAssembler<Credencial, EntityModel<CredencialResposta>> {
    private final CredencialServico servico;

    public CredencialRepresentacao(CredencialServico servico) {
        this.servico = servico;
    }

    @Override
    public EntityModel<CredencialResposta> toModel(Credencial credencial) {
        long id = credencial.getId();
        var controle = methodOn(CredencialControle.class);
        var modelo = EntityModel.of(CredencialResposta.de(credencial),
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarCredencial(id, null)))
                        .andAffordance(afford(controle.excluirCredencial(id))),
                linkTo(controle.listar(null)).withRel("credenciais").expand(), Ligacoes.inicio());
        Long dono = servico.usuarioDe(id);
        if (dono != null) modelo.add(Ligacoes.usuario(dono, "usuario"));
        return modelo;
    }
}
