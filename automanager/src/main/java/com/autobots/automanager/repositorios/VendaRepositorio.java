package com.autobots.automanager.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.autobots.automanager.entidades.Venda;

public interface VendaRepositorio extends JpaRepository<Venda, Long> {
    @Query("""
            select v from Venda v
            where (:empresaId is null or v.empresa.id = :empresaId)
              and (:clienteId is null or v.cliente.id = :clienteId)
              and (:funcionarioId is null or v.funcionario.id = :funcionarioId)
              and (:veiculoId is null or v.veiculo.id = :veiculoId)
            order by v.id""")
    List<Venda> filtrar(Long empresaId, Long clienteId, Long funcionarioId, Long veiculoId);

    boolean existsByIdentificacaoAndIdNot(String identificacao, long id);

    boolean existsByClienteIdOrFuncionarioId(long clienteId, long funcionarioId);

    boolean existsByClienteId(long clienteId);

    boolean existsByFuncionarioId(long funcionarioId);

    boolean existsByVeiculoId(long veiculoId);

    @Query("select count(v) > 0 from Venda v where v.empresa.id = :empresaId and (v.cliente.id = :usuarioId or v.funcionario.id = :usuarioId)")
    boolean usuarioParticipaNaEmpresa(long empresaId, long usuarioId);

    @Query("select count(v) > 0 from Venda v join v.mercadorias m where m.id = :mercadoriaId")
    boolean contemMercadoria(long mercadoriaId);

    @Query("select count(v) > 0 from Venda v join v.servicos s where s.id = :servicoId")
    boolean contemServico(long servicoId);

    List<Venda> findByEmpresaId(long empresaId);
}
