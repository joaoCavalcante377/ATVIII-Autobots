package com.autobots.automanager.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.autobots.automanager.entidades.Servico;

public interface ServicoRepositorio extends JpaRepository<Servico, Long> {
    @Query("select s from Servico s where (:empresaId is null or s.empresa.id = :empresaId) order by s.id")
    List<Servico> filtrar(Long empresaId);

    List<Servico> findByEmpresaId(long empresaId);
}
