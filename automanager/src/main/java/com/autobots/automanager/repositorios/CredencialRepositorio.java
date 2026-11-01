package com.autobots.automanager.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.autobots.automanager.entidades.Credencial;

public interface CredencialRepositorio extends JpaRepository<Credencial, Long> {
    @Query("select c from Usuario u join u.credenciais c where u.id = :usuarioId order by c.id")
    List<Credencial> doUsuario(long usuarioId);

    @Query("select count(c) > 0 from CredencialUsuarioSenha c where lower(c.nomeUsuario) = lower(:nome) and c.id <> :ignorar")
    boolean nomeUsuarioEmUso(String nome, long ignorar);

    @Query("select count(c) > 0 from CredencialCodigoBarra c where c.codigo = :codigo and c.id <> :ignorar")
    boolean codigoEmUso(long codigo, long ignorar);
}
