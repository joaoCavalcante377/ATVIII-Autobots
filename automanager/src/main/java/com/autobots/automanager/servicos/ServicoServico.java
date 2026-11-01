package com.autobots.automanager.servicos;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.ServicoRequisicao;

/** Servicos de manutencao oferecidos por uma empresa. */
@Service
@Transactional
public class ServicoServico {
    private final ServicoRepositorio repositorio;
    private final VendaRepositorio vendas;
    private final EmpresaServico empresaServico;

    public ServicoServico(ServicoRepositorio repositorio, VendaRepositorio vendas, EmpresaServico empresaServico) {
        this.repositorio = repositorio;
        this.vendas = vendas;
        this.empresaServico = empresaServico;
    }

    @Transactional(readOnly = true)
    public List<Servico> listar(Long empresaId) {
        if (empresaId != null) empresaServico.buscar(empresaId);
        return repositorio.filtrar(empresaId);
    }

    @Transactional(readOnly = true)
    public Servico buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Servico nao encontrado"));
    }

    public Servico criar(long empresaId, ServicoRequisicao dados) {
        Servico servico = new Servico();
        servico.setEmpresa(empresaServico.buscar(empresaId));
        return repositorio.saveAndFlush(aplicar(servico, dados));
    }

    public Servico atualizar(long id, ServicoRequisicao dados) {
        return repositorio.saveAndFlush(aplicar(buscar(id), dados));
    }

    private static Servico aplicar(Servico servico, ServicoRequisicao dados) {
        servico.setNome(dados.getNome().strip());
        servico.setDescricao(Conversor.texto(dados.getDescricao()));
        servico.setValor(dados.getValor());
        return servico;
    }

    public void excluir(long id) {
        Servico servico = buscar(id);
        if (vendas.contemServico(id)) throw Erros.conflito("O servico consta em vendas e nao pode ser excluido");
        repositorio.delete(servico);
        repositorio.flush();
    }
}
