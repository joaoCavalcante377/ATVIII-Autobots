package com.autobots.automanager.servicos;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.requisicoes.*;

/** Copia os dados das requisicoes para as entidades, normalizando espacos e formatos. */
public final class Conversor {
    private Conversor() {
    }

    static String texto(String valor) {
        if (valor == null) return null;
        String limpo = valor.strip();
        return limpo.isEmpty() ? null : limpo;
    }

    static <R, E> void adicionar(Collection<E> destino, List<R> origem, Function<R, E> conversao) {
        if (origem != null) origem.stream().map(conversao).forEach(destino::add);
    }

    public static Telefone telefone(TelefoneRequisicao r) {
        return aplicar(new Telefone(), r);
    }

    public static Telefone aplicar(Telefone t, TelefoneRequisicao r) {
        t.setDdd(r.getDdd().strip());
        t.setNumero(r.getNumero().strip());
        return t;
    }

    public static Email email(EmailRequisicao r) {
        return aplicar(new Email(), r);
    }

    public static Email aplicar(Email e, EmailRequisicao r) {
        e.setEndereco(r.getEndereco().strip().toLowerCase(Locale.ROOT));
        return e;
    }

    public static Endereco endereco(EnderecoRequisicao r) {
        return r == null ? null : aplicar(new Endereco(), r);
    }

    public static Endereco aplicar(Endereco e, EnderecoRequisicao r) {
        e.setEstado(texto(r.getEstado()));
        e.setCidade(texto(r.getCidade()));
        e.setBairro(texto(r.getBairro()));
        e.setRua(texto(r.getRua()));
        e.setNumero(texto(r.getNumero()));
        e.setCodigoPostal(texto(r.getCodigoPostal()));
        e.setInformacoesAdicionais(texto(r.getInformacoesAdicionais()));
        return e;
    }

    public static Documento documento(DocumentoRequisicao r) {
        return aplicar(new Documento(), r);
    }

    public static Documento aplicar(Documento d, DocumentoRequisicao r) {
        d.setTipo(r.getTipo());
        d.setNumero(r.getNumero().strip());
        d.setDataEmissao(r.getDataEmissao());
        return d;
    }

    public static String placa(String placa) {
        return placa.strip().toUpperCase(Locale.ROOT);
    }

    static <T> Set<T> semNulos(Set<T> valores) {
        return valores == null ? Set.of() : valores;
    }
}
