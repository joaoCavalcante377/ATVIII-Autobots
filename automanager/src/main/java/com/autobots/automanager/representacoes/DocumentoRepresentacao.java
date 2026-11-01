package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.servicos.DocumentoServico;

@Component
public class DocumentoRepresentacao implements RepresentationModelAssembler<Documento, EntityModel<Documento>> {
    private final DocumentoServico servico;

    public DocumentoRepresentacao(DocumentoServico servico) {
        this.servico = servico;
    }

    @Override
    public EntityModel<Documento> toModel(Documento documento) {
        long id = documento.getId();
        var controle = methodOn(DocumentoControle.class);
        var modelo = EntityModel.of(documento,
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarDocumento(id, null)))
                        .andAffordance(afford(controle.excluirDocumento(id))),
                linkTo(controle.listar(null)).withRel("documentos").expand(), Ligacoes.inicio());
        servico.usuarioDe(id).ifPresent(dono -> modelo.add(Ligacoes.usuario(dono, "usuario")));
        return modelo;
    }
}
