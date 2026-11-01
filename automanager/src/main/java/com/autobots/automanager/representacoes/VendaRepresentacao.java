package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.*;
import com.autobots.automanager.entidades.Venda;

@Component
public class VendaRepresentacao implements RepresentationModelAssembler<Venda, EntityModel<VendaResposta>> {
    @Override
    public EntityModel<VendaResposta> toModel(Venda venda) {
        long id = venda.getId();
        var controle = methodOn(VendaControle.class);
        var modelo = EntityModel.of(VendaResposta.de(venda),
                linkTo(controle.buscar(id)).withSelfRel().expand()
                        .andAffordance(afford(controle.atualizarVenda(id, null)))
                        .andAffordance(afford(controle.excluirVenda(id))),
                linkTo(controle.listar(null, null, null, null)).withRel("vendas").expand(),
                Ligacoes.empresa(venda.getEmpresa().getId(), "empresa"),
                Ligacoes.usuario(venda.getCliente().getId(), "cliente"),
                Ligacoes.usuario(venda.getFuncionario().getId(), "funcionario"), Ligacoes.inicio());
        if (venda.getVeiculo() != null) {
            modelo.add(linkTo(methodOn(VeiculoControle.class).buscar(venda.getVeiculo().getId())).withRel("veiculo").expand());
        }
        venda.getMercadorias().forEach(m ->
                modelo.add(linkTo(methodOn(MercadoriaControle.class).buscar(m.getId())).withRel("mercadoria").expand()));
        venda.getServicos().forEach(s ->
                modelo.add(linkTo(methodOn(ServicoControle.class).buscar(s.getId())).withRel("servico").expand()));
        return modelo;
    }
}
