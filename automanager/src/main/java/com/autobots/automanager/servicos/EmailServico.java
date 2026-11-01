package com.autobots.automanager.servicos;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.EmailRequisicao;

@Service
@Transactional
public class EmailServico {
    private final EmailRepositorio repositorio;
    private final UsuarioRepositorio usuarios;
    private final UsuarioServico usuarioServico;

    public EmailServico(EmailRepositorio repositorio, UsuarioRepositorio usuarios, UsuarioServico usuarioServico) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
        this.usuarioServico = usuarioServico;
    }

    @Transactional(readOnly = true)
    public List<Email> listar(Long usuarioId) {
        if (usuarioId == null) return repositorio.findAll(org.springframework.data.domain.Sort.by("id"));
        return usuarioServico.buscar(usuarioId).getEmails().stream().sorted(Comparator.comparing(Email::getId)).toList();
    }

    @Transactional(readOnly = true)
    public Email buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("E-mail nao encontrado"));
    }

    @Transactional(readOnly = true)
    public Optional<Long> usuarioDe(long id) {
        return usuarios.idPorEmail(id);
    }

    public Email criar(long usuarioId, EmailRequisicao dados) {
        Usuario usuario = usuarioServico.buscar(usuarioId);
        Email email = repositorio.saveAndFlush(Conversor.email(dados));
        usuario.getEmails().add(email);
        usuarios.saveAndFlush(usuario);
        return email;
    }

    public Email atualizar(long id, EmailRequisicao dados) {
        return repositorio.saveAndFlush(Conversor.aplicar(buscar(id), dados));
    }

    public void excluir(long id) {
        Email email = buscar(id);
        Optional<Long> usuario = usuarioDe(id);
        if (usuario.isPresent()) usuarioServico.buscar(usuario.get()).getEmails().remove(email);
        else repositorio.delete(email);
        repositorio.flush();
    }
}
