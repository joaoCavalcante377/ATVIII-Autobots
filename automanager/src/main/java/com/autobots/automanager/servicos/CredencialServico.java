package com.autobots.automanager.servicos;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.TipoCredencial;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.CredencialRequisicao;

/**
 * Credenciais de acesso dos usuarios. A senha nunca e armazenada nem devolvida em texto: guarda-se apenas o hash.
 * A autenticacao em si faz parte da futura "atualizacao de seguranca".
 */
@Service
@Transactional
public class CredencialServico {
    private final CredencialRepositorio repositorio;
    private final UsuarioRepositorio usuarios;
    private final UsuarioServico usuarioServico;
    private final PasswordEncoder codificador;

    public CredencialServico(CredencialRepositorio repositorio, UsuarioRepositorio usuarios, UsuarioServico usuarioServico,
            PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
        this.usuarioServico = usuarioServico;
        this.codificador = codificador;
    }

    public static TipoCredencial tipo(Credencial credencial) {
        return credencial instanceof CredencialCodigoBarra ? TipoCredencial.CODIGO_BARRA : TipoCredencial.USUARIO_SENHA;
    }

    @Transactional(readOnly = true)
    public List<Credencial> listar(Long usuarioId) {
        if (usuarioId == null) return repositorio.findAll(org.springframework.data.domain.Sort.by("id"));
        usuarioServico.buscar(usuarioId);
        return repositorio.doUsuario(usuarioId);
    }

    @Transactional(readOnly = true)
    public Credencial buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Credencial nao encontrada"));
    }

    @Transactional(readOnly = true)
    public Long usuarioDe(long id) {
        return usuarios.donoDaCredencial(id).map(Usuario::getId).orElse(null);
    }

    public Credencial criar(long usuarioId, CredencialRequisicao dados) {
        Usuario usuario = usuarioServico.buscar(usuarioId);
        Credencial credencial = switch (dados.getTipo()) {
            case USUARIO_SENHA -> new CredencialUsuarioSenha();
            case CODIGO_BARRA -> new CredencialCodigoBarra();
        };
        if (credencial instanceof CredencialUsuarioSenha && dados.getSenha() == null) {
            throw Erros.invalido("Informe a senha da credencial");
        }
        aplicar(credencial, dados, 0L);
        credencial.setCriacao(new Date());
        credencial = repositorio.saveAndFlush(credencial);
        usuario.getCredenciais().add(credencial);
        usuarios.saveAndFlush(usuario);
        return credencial;
    }

    public Credencial atualizar(long id, CredencialRequisicao dados) {
        Credencial credencial = buscar(id);
        if (tipo(credencial) != dados.getTipo()) throw Erros.invalido("O tipo da credencial nao pode ser alterado");
        aplicar(credencial, dados, id);
        return repositorio.saveAndFlush(credencial);
    }

    private void aplicar(Credencial credencial, CredencialRequisicao dados, long id) {
        if (credencial instanceof CredencialUsuarioSenha c) {
            if (dados.getCodigo() != null) throw Erros.invalido("Credencial USUARIO_SENHA nao usa codigo");
            if (dados.getNomeUsuario() == null || dados.getNomeUsuario().isBlank()) throw Erros.invalido("Informe o nomeUsuario");
            String nome = dados.getNomeUsuario().strip().toLowerCase(Locale.ROOT);
            if (repositorio.nomeUsuarioEmUso(nome, id)) throw Erros.conflito("Nome de usuario ja esta em uso");
            c.setNomeUsuario(nome);
            if (dados.getSenha() != null) c.setSenha(codificador.encode(dados.getSenha()));
        } else if (credencial instanceof CredencialCodigoBarra c) {
            if (dados.getNomeUsuario() != null || dados.getSenha() != null) {
                throw Erros.invalido("Credencial CODIGO_BARRA usa somente codigo");
            }
            if (dados.getCodigo() == null) throw Erros.invalido("Informe o codigo de barras");
            if (repositorio.codigoEmUso(dados.getCodigo(), id)) throw Erros.conflito("Codigo de barras ja esta em uso");
            c.setCodigo(dados.getCodigo());
        }
        credencial.setInativo(Boolean.TRUE.equals(dados.getInativo()));
    }

    public void excluir(long id) {
        Credencial credencial = buscar(id);
        var dono = usuarios.donoDaCredencial(id);
        if (dono.isPresent()) dono.get().getCredenciais().remove(credencial);
        else repositorio.delete(credencial);
        repositorio.flush();
    }
}
