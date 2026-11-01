package com.autobots.automanager.servicos;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.VendaRequisicao;

/**
 * Registra vendas de mercadorias e servicos. Cliente, funcionario, mercadorias e servicos devem pertencer a empresa
 * da venda, e o veiculo, quando informado, deve ser do cliente.
 */
@Service
@Transactional
public class VendaServico {
    private final VendaRepositorio repositorio;
    private final EmpresaServico empresaServico;
    private final MercadoriaServico mercadorias;
    private final ServicoServico servicos;
    private final VeiculoServico veiculos;
    private final UsuarioServico usuarios;

    public VendaServico(VendaRepositorio repositorio, EmpresaServico empresaServico, MercadoriaServico mercadorias,
            ServicoServico servicos, VeiculoServico veiculos, UsuarioServico usuarios) {
        this.repositorio = repositorio;
        this.empresaServico = empresaServico;
        this.mercadorias = mercadorias;
        this.servicos = servicos;
        this.veiculos = veiculos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<Venda> listar(Long empresaId, Long clienteId, Long funcionarioId, Long veiculoId) {
        if (empresaId != null) empresaServico.buscar(empresaId);
        if (clienteId != null) usuarios.buscar(clienteId);
        if (funcionarioId != null) usuarios.buscar(funcionarioId);
        if (veiculoId != null) veiculos.buscar(veiculoId);
        return repositorio.filtrar(empresaId, clienteId, funcionarioId, veiculoId);
    }

    @Transactional(readOnly = true)
    public Venda buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Venda nao encontrada"));
    }

    public Venda criar(long empresaId, VendaRequisicao dados) {
        Venda venda = new Venda();
        venda.setEmpresa(empresaServico.buscar(empresaId));
        venda.setCadastro(new Date());
        return repositorio.saveAndFlush(aplicar(venda, dados, 0L));
    }

    public Venda atualizar(long id, VendaRequisicao dados) {
        return repositorio.saveAndFlush(aplicar(buscar(id), dados, id));
    }

    private Venda aplicar(Venda venda, VendaRequisicao dados, long id) {
        long empresaId = venda.getEmpresa().getId();
        Set<Long> mercadoriaIds = Conversor.semNulos(dados.getMercadoriaIds());
        Set<Long> servicoIds = Conversor.semNulos(dados.getServicoIds());
        if (mercadoriaIds.isEmpty() && servicoIds.isEmpty()) {
            throw Erros.invalido("A venda precisa de pelo menos uma mercadoria ou um servico");
        }
        String identificacao = Conversor.texto(dados.getIdentificacao());
        if (identificacao == null) {
            identificacao = venda.getIdentificacao() != null ? venda.getIdentificacao()
                    : UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        }
        if (repositorio.existsByIdentificacaoAndIdNot(identificacao, id)) {
            throw Erros.conflito("Ja existe venda com a identificacao " + identificacao);
        }
        Usuario cliente = empresaServico.membro(empresaId, dados.getClienteId(), PerfilUsuario.CLIENTE, "Cliente");
        Usuario funcionario = empresaServico.membro(empresaId, dados.getFuncionarioId(), PerfilUsuario.FUNCIONARIO, "Funcionario");
        Veiculo veiculo = null;
        if (dados.getVeiculoId() != null) {
            veiculo = veiculos.buscar(dados.getVeiculoId());
            if (!veiculo.getProprietario().getId().equals(cliente.getId())) {
                throw Erros.invalido("O veiculo " + veiculo.getId() + " nao pertence ao cliente " + cliente.getId());
            }
        }
        Set<Mercadoria> itens = new HashSet<>();
        for (Long mercadoriaId : mercadoriaIds) {
            Mercadoria mercadoria = mercadorias.buscar(mercadoriaId);
            if (!mercadoria.getEmpresa().getId().equals(empresaId)) {
                throw Erros.invalido("A mercadoria " + mercadoriaId + " nao pertence a empresa " + empresaId);
            }
            itens.add(mercadoria);
        }
        Set<Servico> prestados = new HashSet<>();
        for (Long servicoId : servicoIds) {
            Servico servico = servicos.buscar(servicoId);
            if (!servico.getEmpresa().getId().equals(empresaId)) {
                throw Erros.invalido("O servico " + servicoId + " nao pertence a empresa " + empresaId);
            }
            prestados.add(servico);
        }
        venda.setIdentificacao(identificacao);
        venda.setCliente(cliente);
        venda.setFuncionario(funcionario);
        venda.setVeiculo(veiculo);
        venda.getMercadorias().clear();
        venda.getMercadorias().addAll(itens);
        venda.getServicos().clear();
        venda.getServicos().addAll(prestados);
        return venda;
    }

    public void excluir(long id) {
        repositorio.delete(buscar(id));
        repositorio.flush();
    }
}
