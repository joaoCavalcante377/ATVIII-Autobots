package com.autobots.automanager.repositorios;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.autobots.automanager.entidades.Empresa;

public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {
    @Query("select e.id from Empresa e join e.telefones t where t.id = :id")
    Optional<Long> idPorTelefone(long id);

    @Query("select e.id from Empresa e where e.endereco.id = :id")
    Optional<Long> idPorEndereco(long id);

    @Query("select e from Empresa e join e.usuarios u where u.id = :usuarioId order by e.id")
    List<Empresa> associadasAoUsuario(long usuarioId);

    @Query("select count(e) > 0 from Empresa e join e.usuarios u where e.id = :empresaId and u.id = :usuarioId")
    boolean possuiUsuario(long empresaId, long usuarioId);
}
