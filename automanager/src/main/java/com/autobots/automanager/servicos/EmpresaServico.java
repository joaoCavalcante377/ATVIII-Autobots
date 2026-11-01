package com.autobots.automanager.servicos;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.*;

@Service
@Transactional
public class EmpresaServico {
    private final EmpresaRepositorio repositorio;
    private final UsuarioRepositorio usuarios;
    private final VendaRepositorio vendas;
    private final MercadoriaRepositorio mercadorias;
    private final ServicoRepositorio servicos;

    public EmpresaServico(EmpresaRepositorio repositorio, UsuarioRepositorio usuarios, VendaRepositorio vendas,
            MercadoriaRepositorio mercadorias, ServicoRepositorio servicos) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
        this.vendas = vendas;
        this.mercadorias = mercadorias;
        this.servicos = servicos;
    }

    @Transactional(readOnly = true)
    public List<Empresa> listar(Long usuarioId) {
        if (usuarioId == null) return repositorio.findAll(org.springframework.data.domain.Sort.by("id"));
        if (!usuarios.existsById(usuarioId)) throw Erros.naoEncontrado("Usuario nao encontrado");
        return repositorio.associadasAoUsuario(usuarioId);
    }

    @Transactional(readOnly = true)
    public Empresa buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Empresa nao encontrada"));
    }

    public Empresa criar(EmpresaRequisicao dados) {
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial(dados.getRazaoSocial().strip());
        empresa.setNomeFantasia(Conversor.texto(dados.getNomeFantasia()));
        empresa.setCadastro(new Date());
        empresa.setEndereco(Conversor.endereco(dados.getEndereco()));
        Conversor.adicionar(empresa.getTelefones(), dados.getTelefones(), Conversor::telefone);
        return repositorio.saveAndFlush(empresa);
    }

    public Empresa atualizar(long id, EmpresaAtualizacao dados) {
        Empresa empresa = buscar(id);
        empresa.setRazaoSocial(dados.getRazaoSocial().strip());
        empresa.setNomeFantasia(Conversor.texto(dados.getNomeFantasia()));
        return repositorio.saveAndFlush(empresa);
    }

    /** Exclui a empresa com suas vendas, mercadorias e servicos; os usuarios sao apenas desassociados. */
    public void excluir(long id) {
        Empresa empresa = buscar(id);
        vendas.deleteAll(vendas.findByEmpresaId(id));
        vendas.flush();
        mercadorias.deleteAll(mercadorias.findByEmpresaId(id));
        servicos.deleteAll(servicos.findByEmpresaId(id));
        empresa.getUsuarios().clear();
        repositorio.delete(empresa);
        repositorio.flush();
    }

    public Usuario associar(long empresaId, long usuarioId) {
        Empresa empresa = buscar(empresaId);
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow(() -> Erros.naoEncontrado("Usuario nao encontrado"));
        empresa.getUsuarios().add(usuario);
        repositorio.saveAndFlush(empresa);
        return usuario;
    }

    public void desassociar(long empresaId, long usuarioId) {
        Empresa empresa = buscar(empresaId);
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow(() -> Erros.naoEncontrado("Usuario nao encontrado"));
        if (!empresa.getUsuarios().contains(usuario)) throw Erros.naoEncontrado("Associacao entre empresa e usuario nao encontrada");
        if (vendas.usuarioParticipaNaEmpresa(empresaId, usuarioId)) {
            throw Erros.conflito("O usuario participa de vendas desta empresa e nao pode ser desassociado");
        }
        if (mercadorias.existsByEmpresaIdAndFornecedorId(empresaId, usuarioId)) {
            throw Erros.conflito("O usuario fornece mercadorias desta empresa e nao pode ser desassociado");
        }
        empresa.getUsuarios().remove(usuario);
        repositorio.saveAndFlush(empresa);
    }

    /** Garante que o usuario esta associado a empresa e possui o perfil esperado. */
    @Transactional(readOnly = true)
    public Usuario membro(long empresaId, long usuarioId, PerfilUsuario perfil, String papel) {
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow(() -> Erros.naoEncontrado(papel + " nao encontrado"));
        if (!repositorio.possuiUsuario(empresaId, usuarioId)) {
            throw Erros.invalido("O " + papel.toLowerCase() + " " + usuarioId + " nao esta associado a empresa " + empresaId);
        }
        if (!usuario.getPerfis().contains(perfil)) {
            throw Erros.invalido("O usuario " + usuarioId + " nao possui o perfil " + perfil);
        }
        return usuario;
    }
}
