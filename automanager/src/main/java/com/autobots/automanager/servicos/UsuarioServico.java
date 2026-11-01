package com.autobots.automanager.servicos;

import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.*;

@Service
@Transactional
public class UsuarioServico {
    private final UsuarioRepositorio repositorio;
    private final EmpresaRepositorio empresas;
    private final VendaRepositorio vendas;
    private final MercadoriaRepositorio mercadorias;
    private final VeiculoRepositorio veiculos;
    private final DocumentoRepositorio documentos;

    public UsuarioServico(UsuarioRepositorio repositorio, EmpresaRepositorio empresas, VendaRepositorio vendas,
            MercadoriaRepositorio mercadorias, VeiculoRepositorio veiculos, DocumentoRepositorio documentos) {
        this.repositorio = repositorio;
        this.empresas = empresas;
        this.vendas = vendas;
        this.mercadorias = mercadorias;
        this.veiculos = veiculos;
        this.documentos = documentos;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar(PerfilUsuario perfil, Long empresaId) {
        if (empresaId != null && !empresas.existsById(empresaId)) throw Erros.naoEncontrado("Empresa nao encontrada");
        return repositorio.filtrar(perfil, empresaId);
    }

    @Transactional(readOnly = true)
    public Usuario buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Usuario nao encontrado"));
    }

    /** Cadastra o usuario e, se empresaId for informado, ja o associa a empresa. */
    public Usuario criar(UsuarioRequisicao dados, Long empresaId) {
        Empresa empresa = empresaId == null ? null
                : empresas.findById(empresaId).orElseThrow(() -> Erros.naoEncontrado("Empresa nao encontrada"));
        if (dados.getDocumentos() != null) {
            var numeros = dados.getDocumentos().stream().map(d -> d.getNumero().strip()).toList();
            if (numeros.stream().distinct().count() != numeros.size()) throw Erros.conflito("Numero de documento repetido");
            numeros.stream().filter(documentos::existsByNumero).findFirst().ifPresent(n -> {
                throw Erros.conflito("Ja existe documento com o numero " + n);
            });
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dados.getNome().strip());
        usuario.setNomeSocial(Conversor.texto(dados.getNomeSocial()));
        usuario.getPerfis().addAll(dados.getPerfis());
        usuario.setEndereco(Conversor.endereco(dados.getEndereco()));
        Conversor.adicionar(usuario.getTelefones(), dados.getTelefones(), Conversor::telefone);
        Conversor.adicionar(usuario.getDocumentos(), dados.getDocumentos(), Conversor::documento);
        Conversor.adicionar(usuario.getEmails(), dados.getEmails(), Conversor::email);
        usuario = repositorio.saveAndFlush(usuario);
        if (empresa != null) {
            empresa.getUsuarios().add(usuario);
            empresas.saveAndFlush(empresa);
        }
        return usuario;
    }

    public Usuario atualizar(long id, UsuarioAtualizacao dados) {
        Usuario usuario = buscar(id);
        var removidos = EnumSet.allOf(PerfilUsuario.class);
        removidos.removeAll(dados.getPerfis());
        removidos.retainAll(usuario.getPerfis());
        if (removidos.contains(PerfilUsuario.CLIENTE) && (vendas.existsByClienteId(id) || veiculos.existsByProprietarioId(id))) {
            throw Erros.conflito("O perfil CLIENTE esta em uso por vendas ou veiculos do usuario");
        }
        if (removidos.contains(PerfilUsuario.FUNCIONARIO) && vendas.existsByFuncionarioId(id)) {
            throw Erros.conflito("O perfil FUNCIONARIO esta em uso por vendas registradas pelo usuario");
        }
        if (removidos.contains(PerfilUsuario.FORNECEDOR) && mercadorias.existsByFornecedorId(id)) {
            throw Erros.conflito("O perfil FORNECEDOR esta em uso por mercadorias fornecidas pelo usuario");
        }
        usuario.setNome(dados.getNome().strip());
        usuario.setNomeSocial(Conversor.texto(dados.getNomeSocial()));
        usuario.getPerfis().clear();
        usuario.getPerfis().addAll(dados.getPerfis());
        return repositorio.saveAndFlush(usuario);
    }

    /**
     * Exclui o usuario com seus dados pessoais, credenciais e veiculos. Usuarios presentes no historico de vendas
     * ou que fornecem mercadorias nao podem ser excluidos.
     */
    public void excluir(long id) {
        Usuario usuario = buscar(id);
        if (vendas.existsByClienteIdOrFuncionarioId(id, id)) {
            throw Erros.conflito("O usuario participa de vendas e nao pode ser excluido");
        }
        if (mercadorias.existsByFornecedorId(id)) {
            throw Erros.conflito("O usuario fornece mercadorias e nao pode ser excluido");
        }
        veiculos.deleteAll(veiculos.findByProprietarioId(id));
        for (Empresa empresa : empresas.associadasAoUsuario(id)) {
            empresa.getUsuarios().remove(usuario);
        }
        repositorio.delete(usuario);
        repositorio.flush();
    }
}
