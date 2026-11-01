package com.autobots.automanager.servicos;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.MercadoriaRequisicao;

@Service
@Transactional
public class MercadoriaServico {
    private final MercadoriaRepositorio repositorio;
    private final VendaRepositorio vendas;
    private final EmpresaServico empresaServico;
    private final UsuarioServico usuarioServico;

    public MercadoriaServico(MercadoriaRepositorio repositorio, VendaRepositorio vendas, EmpresaServico empresaServico,
            UsuarioServico usuarioServico) {
        this.repositorio = repositorio;
        this.vendas = vendas;
        this.empresaServico = empresaServico;
        this.usuarioServico = usuarioServico;
    }

    @Transactional(readOnly = true)
    public List<Mercadoria> listar(Long empresaId, Long fornecedorId) {
        if (empresaId != null) empresaServico.buscar(empresaId);
        if (fornecedorId != null) usuarioServico.buscar(fornecedorId);
        return repositorio.filtrar(empresaId, fornecedorId);
    }

    @Transactional(readOnly = true)
    public Mercadoria buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Mercadoria nao encontrada"));
    }

    public Mercadoria criar(long empresaId, MercadoriaRequisicao dados) {
        Mercadoria mercadoria = new Mercadoria();
        mercadoria.setEmpresa(empresaServico.buscar(empresaId));
        mercadoria.setCadastro(new Date());
        return repositorio.saveAndFlush(aplicar(mercadoria, dados));
    }

    public Mercadoria atualizar(long id, MercadoriaRequisicao dados) {
        return repositorio.saveAndFlush(aplicar(buscar(id), dados));
    }

    private Mercadoria aplicar(Mercadoria mercadoria, MercadoriaRequisicao dados) {
        if (dados.getFabricacao() != null && dados.getValidade() != null && dados.getValidade().isBefore(dados.getFabricacao())) {
            throw Erros.invalido("A validade nao pode ser anterior a fabricacao");
        }
        mercadoria.setFornecedor(dados.getFornecedorId() == null ? null
                : empresaServico.membro(mercadoria.getEmpresa().getId(), dados.getFornecedorId(), PerfilUsuario.FORNECEDOR, "Fornecedor"));
        mercadoria.setNome(dados.getNome().strip());
        mercadoria.setDescricao(Conversor.texto(dados.getDescricao()));
        mercadoria.setQuantidade(dados.getQuantidade());
        mercadoria.setValor(dados.getValor());
        mercadoria.setFabricacao(dados.getFabricacao());
        mercadoria.setValidade(dados.getValidade());
        return mercadoria;
    }

    public void excluir(long id) {
        Mercadoria mercadoria = buscar(id);
        if (vendas.contemMercadoria(id)) throw Erros.conflito("A mercadoria consta em vendas e nao pode ser excluida");
        repositorio.delete(mercadoria);
        repositorio.flush();
    }
}
