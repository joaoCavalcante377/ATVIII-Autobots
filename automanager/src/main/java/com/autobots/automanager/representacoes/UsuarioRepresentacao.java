package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.*;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

@Component
public class UsuarioRepresentacao implements RepresentationModelAssembler<Usuario, EntityModel<Usuario>> {
    @Override
    public EntityModel<Usuario> toModel(Usuario usuario) {
        long id = usuario.getId();
        var controle = methodOn(UsuarioControle.class);
        var modelo = EntityModel.of(usuario,
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarUsuario(id, null)))
                        .andAffordance(afford(controle.excluirUsuario(id))),
                linkTo(controle.listar(null, null)).withRel("usuarios").expand(),
                linkTo(methodOn(EmpresaControle.class).listar(id)).withRel("empresas").expand(),
                linkTo(methodOn(TelefoneControle.class).listar(id, null)).withRel("telefones").expand()
                        .andAffordance(afford(methodOn(TelefoneControle.class).criarTelefone(id, null, null))),
                linkTo(methodOn(DocumentoControle.class).listar(id)).withRel("documentos").expand()
                        .andAffordance(afford(methodOn(DocumentoControle.class).criarDocumento(id, null))),
                linkTo(methodOn(EmailControle.class).listar(id)).withRel("emails").expand()
                        .andAffordance(afford(methodOn(EmailControle.class).criarEmail(id, null))),
                linkTo(methodOn(CredencialControle.class).listar(id)).withRel("credenciais").expand()
                        .andAffordance(afford(methodOn(CredencialControle.class).criarCredencial(id, null))),
                Ligacoes.inicio());
        if (usuario.getEndereco() == null) {
            modelo.add(linkTo(methodOn(EnderecoControle.class).listar(id, null)).withRel("enderecos").expand()
                    .andAffordance(afford(methodOn(EnderecoControle.class).criarEndereco(id, null, null))));
        } else {
            modelo.add(linkTo(methodOn(EnderecoControle.class).buscar(usuario.getEndereco().getId())).withRel("endereco").expand());
        }
        if (usuario.getPerfis().contains(PerfilUsuario.CLIENTE)) {
            modelo.add(linkTo(methodOn(VeiculoControle.class).listar(id)).withRel("veiculos").expand()
                    .andAffordance(afford(methodOn(VeiculoControle.class).criarVeiculo(id, null))));
            modelo.add(linkTo(methodOn(VendaControle.class).listar(null, id, null, null)).withRel("compras").expand());
        }
        if (usuario.getPerfis().contains(PerfilUsuario.FUNCIONARIO)) {
            modelo.add(linkTo(methodOn(VendaControle.class).listar(null, null, id, null)).withRel("vendasRealizadas").expand());
        }
        if (usuario.getPerfis().contains(PerfilUsuario.FORNECEDOR)) {
            modelo.add(linkTo(methodOn(MercadoriaControle.class).listar(null, id)).withRel("mercadoriasFornecidas").expand());
        }
        return modelo;
    }
}
