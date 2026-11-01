package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.*;
import com.autobots.automanager.servicos.UsuarioServico;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControle {
    private final UsuarioServico servico;
    private final UsuarioRepresentacao representacao;

    public UsuarioControle(UsuarioServico servico, UsuarioRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<Usuario>> listar(@RequestParam(required = false) PerfilUsuario perfil,
            @RequestParam(required = false) Long empresaId) {
        var proprio = linkTo(methodOn(UsuarioControle.class).listar(perfil, empresaId)).withSelfRel().expand()
                .andAffordance(afford(methodOn(UsuarioControle.class).criarUsuario(empresaId, null)));
        var registros = servico.listar(perfil, empresaId);
        return empresaId == null ? Colecoes.de(registros, representacao, Usuario.class, proprio)
                : Colecoes.de(registros, representacao, Usuario.class, proprio, Ligacoes.empresa(empresaId, "empresa"));
    }

    @GetMapping("/{id}")
    public EntityModel<Usuario> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    /** Cadastra um usuario; com empresaId ele ja fica associado a empresa. */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<Usuario>> criarUsuario(@RequestParam(required = false) Long empresaId,
            @Valid @RequestBody UsuarioRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(dados, empresaId));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<Usuario> atualizarUsuario(@PathVariable long id, @Valid @RequestBody UsuarioAtualizacao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
