package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.*;
import com.autobots.automanager.entidades.Empresa;

@Component
public class EmpresaRepresentacao implements RepresentationModelAssembler<Empresa, EntityModel<Empresa>> {
    @Override
    public EntityModel<Empresa> toModel(Empresa empresa) {
        long id = empresa.getId();
        var controle = methodOn(EmpresaControle.class);
        var modelo = EntityModel.of(empresa,
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarEmpresa(id, null)))
                        .andAffordance(afford(controle.excluirEmpresa(id))),
                linkTo(controle.listar(null)).withRel("empresas").expand(),
                linkTo(methodOn(UsuarioControle.class).listar(null, id)).withRel("usuarios").expand()
                        .andAffordance(afford(methodOn(UsuarioControle.class).criarUsuario(id, null))),
                linkTo(methodOn(TelefoneControle.class).listar(null, id)).withRel("telefones").expand()
                        .andAffordance(afford(methodOn(TelefoneControle.class).criarTelefone(null, id, null))),
                linkTo(methodOn(MercadoriaControle.class).listar(id, null)).withRel("mercadorias").expand()
                        .andAffordance(afford(methodOn(MercadoriaControle.class).criarMercadoria(id, null))),
                linkTo(methodOn(ServicoControle.class).listar(id)).withRel("servicos").expand()
                        .andAffordance(afford(methodOn(ServicoControle.class).criarServico(id, null))),
                linkTo(methodOn(VendaControle.class).listar(id, null, null, null)).withRel("vendas").expand()
                        .andAffordance(afford(methodOn(VendaControle.class).criarVenda(id, null))),
                Ligacoes.inicio());
        if (empresa.getEndereco() == null) {
            modelo.add(linkTo(methodOn(EnderecoControle.class).listar(null, id)).withRel("enderecos").expand()
                    .andAffordance(afford(methodOn(EnderecoControle.class).criarEndereco(null, id, null))));
        } else {
            modelo.add(linkTo(methodOn(EnderecoControle.class).buscar(empresa.getEndereco().getId())).withRel("endereco").expand());
        }
        return modelo;
    }
}
