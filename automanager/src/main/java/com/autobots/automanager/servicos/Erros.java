package com.autobots.automanager.servicos;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Erros de negocio traduzidos para respostas HTTP (application/problem+json). */
public final class Erros {
    private Erros() {
    }

    public static ResponseStatusException naoEncontrado(String mensagem) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, mensagem);
    }

    public static ResponseStatusException conflito(String detalhe) {
        return new ResponseStatusException(HttpStatus.CONFLICT, detalhe);
    }

    public static ResponseStatusException invalido(String detalhe) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, detalhe);
    }

    /** Exige exatamente um dono (usuario ou empresa) para telefones e enderecos. */
    public static void exigirUmDono(Long usuarioId, Long empresaId) {
        if ((usuarioId == null) == (empresaId == null)) {
            throw invalido("Informe exatamente um dono: usuarioId ou empresaId");
        }
    }
}
