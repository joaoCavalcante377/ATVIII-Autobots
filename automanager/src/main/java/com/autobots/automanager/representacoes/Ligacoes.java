package com.autobots.automanager.representacoes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.Link;
import com.autobots.automanager.controles.*;

/** Links comuns entre os recursos. Os enderecos sao gerados a partir dos controladores e da requisicao atual. */
public final class Ligacoes {
    private Ligacoes() {
    }

    public static Link inicio() {
        return linkTo(methodOn(RaizControle.class).inicio()).withRel("inicio").expand();
    }

    public static Link usuario(long id, String relacao) {
        return linkTo(methodOn(UsuarioControle.class).buscar(id)).withRel(relacao).expand();
    }

    public static Link empresa(long id, String relacao) {
        return linkTo(methodOn(EmpresaControle.class).buscar(id)).withRel(relacao).expand();
    }
}
