package com.autobots.automanager.servicos;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.DocumentoRequisicao;

@Service
@Transactional
public class DocumentoServico {
    private final DocumentoRepositorio repositorio;
    private final UsuarioRepositorio usuarios;
    private final UsuarioServico usuarioServico;

    public DocumentoServico(DocumentoRepositorio repositorio, UsuarioRepositorio usuarios, UsuarioServico usuarioServico) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
        this.usuarioServico = usuarioServico;
    }

    @Transactional(readOnly = true)
    public List<Documento> listar(Long usuarioId) {
        if (usuarioId == null) return repositorio.findAll(org.springframework.data.domain.Sort.by("id"));
        return usuarioServico.buscar(usuarioId).getDocumentos().stream().sorted(Comparator.comparing(Documento::getId)).toList();
    }

    @Transactional(readOnly = true)
    public Documento buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Documento nao encontrado"));
    }

    @Transactional(readOnly = true)
    public Optional<Long> usuarioDe(long id) {
        return usuarios.idPorDocumento(id);
    }

    public Documento criar(long usuarioId, DocumentoRequisicao dados) {
        Usuario usuario = usuarioServico.buscar(usuarioId);
        if (repositorio.existsByNumero(dados.getNumero().strip())) throw Erros.conflito("Ja existe documento com este numero");
        Documento documento = repositorio.saveAndFlush(Conversor.documento(dados));
        usuario.getDocumentos().add(documento);
        usuarios.saveAndFlush(usuario);
        return documento;
    }

    public Documento atualizar(long id, DocumentoRequisicao dados) {
        Documento documento = buscar(id);
        if (repositorio.existsByNumeroAndIdNot(dados.getNumero().strip(), id)) throw Erros.conflito("Ja existe documento com este numero");
        return repositorio.saveAndFlush(Conversor.aplicar(documento, dados));
    }

    public void excluir(long id) {
        Documento documento = buscar(id);
        Optional<Long> usuario = usuarioDe(id);
        if (usuario.isPresent()) usuarioServico.buscar(usuario.get()).getDocumentos().remove(documento);
        else repositorio.delete(documento);
        repositorio.flush();
    }
}
