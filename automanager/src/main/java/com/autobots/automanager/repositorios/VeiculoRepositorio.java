package com.autobots.automanager.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.autobots.automanager.entidades.Veiculo;

public interface VeiculoRepositorio extends JpaRepository<Veiculo, Long> {
    @Query("select v from Veiculo v where (:proprietarioId is null or v.proprietario.id = :proprietarioId) order by v.id")
    List<Veiculo> filtrar(Long proprietarioId);

    boolean existsByPlacaAndIdNot(String placa, long id);

    boolean existsByProprietarioId(long proprietarioId);

    List<Veiculo> findByProprietarioId(long proprietarioId);
}
