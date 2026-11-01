package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.servicos.EnderecoServico;

@Component
public class EnderecoRepresentacao implements RepresentationModelAssembler<Endereco, EntityModel<Endereco>> {
    private final EnderecoServico servico;

    public EnderecoRepresentacao(EnderecoServico servico) {
        this.servico = servico;
    }

    @Override
    public EntityModel<Endereco> toModel(Endereco endereco) {
        long id = endereco.getId();
        var controle = methodOn(EnderecoControle.class);
        var modelo = EntityModel.of(endereco,
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarEndereco(id, null)))
                        .andAffordance(afford(controle.excluirEndereco(id))),
                linkTo(controle.listar(null, null)).withRel("enderecos").expand(), Ligacoes.inicio());
        servico.usuarioDe(id).ifPresent(dono -> modelo.add(Ligacoes.usuario(dono, "usuario")));
        servico.empresaDe(id).ifPresent(dono -> modelo.add(Ligacoes.empresa(dono, "empresa")));
        return modelo;
    }
}
