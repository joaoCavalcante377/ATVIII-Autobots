package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.*;

@RestController
public class RaizControle {
    @GetMapping("/")
    public RepresentationModel<?> inicio() {
        return new RepresentationModel<>().add(
                linkTo(methodOn(RaizControle.class).inicio()).withSelfRel().expand(),
                linkTo(methodOn(EmpresaControle.class).listar(null)).withRel("empresas").expand(),
                linkTo(methodOn(UsuarioControle.class).listar(null, null)).withRel("usuarios").expand(),
                linkTo(methodOn(VeiculoControle.class).listar(null)).withRel("veiculos").expand(),
                linkTo(methodOn(MercadoriaControle.class).listar(null, null)).withRel("mercadorias").expand(),
                linkTo(methodOn(ServicoControle.class).listar(null)).withRel("servicos").expand(),
                linkTo(methodOn(VendaControle.class).listar(null, null, null, null)).withRel("vendas").expand(),
                linkTo(methodOn(TelefoneControle.class).listar(null, null)).withRel("telefones").expand(),
                linkTo(methodOn(EnderecoControle.class).listar(null, null)).withRel("enderecos").expand(),
                linkTo(methodOn(DocumentoControle.class).listar(null)).withRel("documentos").expand(),
                linkTo(methodOn(EmailControle.class).listar(null)).withRel("emails").expand(),
                linkTo(methodOn(CredencialControle.class).listar(null)).withRel("credenciais").expand());
    }
}
