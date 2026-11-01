package com.autobots.automanager.servicos;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.EnderecoRequisicao;

/** Cada usuario ou empresa possui no maximo um endereco. */
@Service
@Transactional
public class EnderecoServico {
    private final EnderecoRepositorio repositorio;
    private final UsuarioRepositorio usuarios;
    private final EmpresaRepositorio empresas;
    private final UsuarioServico usuarioServico;
    private final EmpresaServico empresaServico;

    public EnderecoServico(EnderecoRepositorio repositorio, UsuarioRepositorio usuarios, EmpresaRepositorio empresas,
            UsuarioServico usuarioServico, EmpresaServico empresaServico) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
        this.empresas = empresas;
        this.usuarioServico = usuarioServico;
        this.empresaServico = empresaServico;
    }

    @Transactional(readOnly = true)
    public List<Endereco> listar(Long usuarioId, Long empresaId) {
        if (usuarioId != null && empresaId != null) throw Erros.invalido("Filtre por usuarioId ou por empresaId");
        Endereco endereco;
        if (usuarioId != null) endereco = usuarioServico.buscar(usuarioId).getEndereco();
        else if (empresaId != null) endereco = empresaServico.buscar(empresaId).getEndereco();
        else return repositorio.findAll(org.springframework.data.domain.Sort.by("id"));
        return endereco == null ? List.of() : List.of(endereco);
    }

    @Transactional(readOnly = true)
    public Endereco buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Endereco nao encontrado"));
    }

    @Transactional(readOnly = true)
    public Optional<Long> usuarioDe(long id) {
        return usuarios.idPorEndereco(id);
    }

    @Transactional(readOnly = true)
    public Optional<Long> empresaDe(long id) {
        return empresas.idPorEndereco(id);
    }

    public Endereco criar(Long usuarioId, Long empresaId, EnderecoRequisicao dados) {
        Erros.exigirUmDono(usuarioId, empresaId);
        if (usuarioId != null) {
            Usuario usuario = usuarioServico.buscar(usuarioId);
            if (usuario.getEndereco() != null) throw Erros.conflito("O usuario ja possui endereco; use PUT para alterar");
            usuario.setEndereco(Conversor.endereco(dados));
            return usuarios.saveAndFlush(usuario).getEndereco();
        }
        Empresa empresa = empresaServico.buscar(empresaId);
        if (empresa.getEndereco() != null) throw Erros.conflito("A empresa ja possui endereco; use PUT para alterar");
        empresa.setEndereco(Conversor.endereco(dados));
        return empresas.saveAndFlush(empresa).getEndereco();
    }

    public Endereco atualizar(long id, EnderecoRequisicao dados) {
        return repositorio.saveAndFlush(Conversor.aplicar(buscar(id), dados));
    }

    public void excluir(long id) {
        Endereco endereco = buscar(id);
        Optional<Long> usuario = usuarioDe(id);
        Optional<Long> empresa = empresaDe(id);
        if (usuario.isPresent()) usuarioServico.buscar(usuario.get()).setEndereco(null);
        else if (empresa.isPresent()) empresaServico.buscar(empresa.get()).setEndereco(null);
        else repositorio.delete(endereco);
        repositorio.flush();
    }
}
