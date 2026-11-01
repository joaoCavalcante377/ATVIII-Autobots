package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.servicos.EmailServico;

@Component
public class EmailRepresentacao implements RepresentationModelAssembler<Email, EntityModel<Email>> {
    private final EmailServico servico;

    public EmailRepresentacao(EmailServico servico) {
        this.servico = servico;
    }

    @Override
    public EntityModel<Email> toModel(Email email) {
        long id = email.getId();
        var controle = methodOn(EmailControle.class);
        var modelo = EntityModel.of(email,
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarEmail(id, null)))
                        .andAffordance(afford(controle.excluirEmail(id))),
                linkTo(controle.listar(null)).withRel("emails").expand(), Ligacoes.inicio());
        servico.usuarioDe(id).ifPresent(dono -> modelo.add(Ligacoes.usuario(dono, "usuario")));
        return modelo;
    }
}
