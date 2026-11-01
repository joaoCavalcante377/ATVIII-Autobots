package com.autobots.automanager.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.autobots.automanager.entidades.Mercadoria;

public interface MercadoriaRepositorio extends JpaRepository<Mercadoria, Long> {
    @Query("""
            select m from Mercadoria m
            where (:empresaId is null or m.empresa.id = :empresaId)
              and (:fornecedorId is null or m.fornecedor.id = :fornecedorId)
            order by m.id""")
    List<Mercadoria> filtrar(Long empresaId, Long fornecedorId);

    boolean existsByFornecedorId(long fornecedorId);

    boolean existsByEmpresaIdAndFornecedorId(long empresaId, long fornecedorId);

    List<Mercadoria> findByEmpresaId(long empresaId);
}
