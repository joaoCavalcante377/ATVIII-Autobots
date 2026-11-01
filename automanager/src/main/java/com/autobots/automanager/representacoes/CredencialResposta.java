package com.autobots.automanager.representacoes;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.server.core.Relation;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.TipoCredencial;
import com.autobots.automanager.servicos.CredencialServico;

/** Dados publicos da credencial. A senha (ou seu hash) nunca e devolvida. */
@Relation(itemRelation = "credencial", collectionRelation = "credenciais")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CredencialResposta(Long id, TipoCredencial tipo, String nomeUsuario, Long codigo, Date criacao,
        Date ultimoAcesso, boolean inativo) {
    public static CredencialResposta de(Credencial c) {
        String nome = c instanceof CredencialUsuarioSenha s ? s.getNomeUsuario() : null;
        Long codigo = c instanceof CredencialCodigoBarra b ? b.getCodigo() : null;
        return new CredencialResposta(c.getId(), CredencialServico.tipo(c), nome, codigo, c.getCriacao(), c.getUltimoAcesso(),
                c.isInativo());
    }
}
