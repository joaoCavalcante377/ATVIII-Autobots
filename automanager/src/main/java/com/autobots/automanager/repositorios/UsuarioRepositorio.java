package com.autobots.automanager.repositorios;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    @Query("""
            select distinct u from Usuario u
            where (:perfil is null or :perfil member of u.perfis)
              and (:empresaId is null or exists (select 1 from Empresa e join e.usuarios eu where e.id = :empresaId and eu = u))
            order by u.id""")
    List<Usuario> filtrar(PerfilUsuario perfil, Long empresaId);

    @Query("select u.id from Usuario u join u.telefones t where t.id = :id")
    Optional<Long> idPorTelefone(long id);

    @Query("select u.id from Usuario u where u.endereco.id = :id")
    Optional<Long> idPorEndereco(long id);

    @Query("select u.id from Usuario u join u.documentos d where d.id = :id")
    Optional<Long> idPorDocumento(long id);

    @Query("select u.id from Usuario u join u.emails m where m.id = :id")
    Optional<Long> idPorEmail(long id);

    @Query("select u from Usuario u join u.credenciais c where c.id = :id")
    Optional<Usuario> donoDaCredencial(long id);
}
