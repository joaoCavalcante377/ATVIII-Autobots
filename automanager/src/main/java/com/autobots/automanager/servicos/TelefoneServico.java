package com.autobots.automanager.servicos;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.TelefoneRequisicao;

/** Telefones pertencem a um usuario ou a uma empresa. */
@Service
@Transactional
public class TelefoneServico {
    private final TelefoneRepositorio repositorio;
    private final UsuarioRepositorio usuarios;
    private final EmpresaRepositorio empresas;
    private final UsuarioServico usuarioServico;
    private final EmpresaServico empresaServico;

    public TelefoneServico(TelefoneRepositorio repositorio, UsuarioRepositorio usuarios, EmpresaRepositorio empresas,
            UsuarioServico usuarioServico, EmpresaServico empresaServico) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
        this.empresas = empresas;
        this.usuarioServico = usuarioServico;
        this.empresaServico = empresaServico;
    }

    @Transactional(readOnly = true)
    public List<Telefone> listar(Long usuarioId, Long empresaId) {
        if (usuarioId != null && empresaId != null) throw Erros.invalido("Filtre por usuarioId ou por empresaId");
        if (usuarioId != null) return ordenar(usuarioServico.buscar(usuarioId).getTelefones());
        if (empresaId != null) return ordenar(empresaServico.buscar(empresaId).getTelefones());
        return repositorio.findAll(org.springframework.data.domain.Sort.by("id"));
    }

    private static List<Telefone> ordenar(java.util.Collection<Telefone> telefones) {
        return telefones.stream().sorted(Comparator.comparing(Telefone::getId)).toList();
    }

    @Transactional(readOnly = true)
    public Telefone buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Telefone nao encontrado"));
    }

    @Transactional(readOnly = true)
    public Optional<Long> usuarioDe(long id) {
        return usuarios.idPorTelefone(id);
    }

    @Transactional(readOnly = true)
    public Optional<Long> empresaDe(long id) {
        return empresas.idPorTelefone(id);
    }

    public Telefone criar(Long usuarioId, Long empresaId, TelefoneRequisicao dados) {
        Erros.exigirUmDono(usuarioId, empresaId);
        Telefone telefone = repositorio.saveAndFlush(Conversor.telefone(dados));
        if (usuarioId != null) {
            Usuario usuario = usuarioServico.buscar(usuarioId);
            usuario.getTelefones().add(telefone);
            usuarios.saveAndFlush(usuario);
        } else {
            Empresa empresa = empresaServico.buscar(empresaId);
            empresa.getTelefones().add(telefone);
            empresas.saveAndFlush(empresa);
        }
        return telefone;
    }

    public Telefone atualizar(long id, TelefoneRequisicao dados) {
        return repositorio.saveAndFlush(Conversor.aplicar(buscar(id), dados));
    }

    public void excluir(long id) {
        Telefone telefone = buscar(id);
        Optional<Long> usuario = usuarioDe(id);
        Optional<Long> empresa = empresaDe(id);
        if (usuario.isPresent()) usuarioServico.buscar(usuario.get()).getTelefones().remove(telefone);
        else if (empresa.isPresent()) empresaServico.buscar(empresa.get()).getTelefones().remove(telefone);
        else repositorio.delete(telefone);
        repositorio.flush();
    }
}
