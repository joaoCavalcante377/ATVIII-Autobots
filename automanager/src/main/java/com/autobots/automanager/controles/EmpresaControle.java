package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.*;
import com.autobots.automanager.servicos.EmpresaServico;

@RestController
@RequestMapping("/empresas")
public class EmpresaControle {
    private final EmpresaServico servico;
    private final EmpresaRepresentacao representacao;
    private final UsuarioRepresentacao usuarios;

    public EmpresaControle(EmpresaServico servico, EmpresaRepresentacao representacao, UsuarioRepresentacao usuarios) {
        this.servico = servico;
        this.representacao = representacao;
        this.usuarios = usuarios;
    }

    @GetMapping
    public CollectionModel<EntityModel<Empresa>> listar(@RequestParam(required = false) Long usuarioId) {
        var proprio = linkTo(methodOn(EmpresaControle.class).listar(usuarioId)).withSelfRel().expand()
                .andAffordance(afford(methodOn(EmpresaControle.class).criarEmpresa(null)));
        return usuarioId == null ? Colecoes.de(servico.listar(null), representacao, Empresa.class, proprio)
                : Colecoes.de(servico.listar(usuarioId), representacao, Empresa.class, proprio, Ligacoes.usuario(usuarioId, "usuario"));
    }

    @GetMapping("/{id}")
    public EntityModel<Empresa> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<Empresa>> criarEmpresa(@Valid @RequestBody EmpresaRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<Empresa> atualizarEmpresa(@PathVariable long id, @Valid @RequestBody EmpresaAtualizacao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirEmpresa(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Associa um usuario existente (cliente, funcionario ou fornecedor) a empresa. A operacao e idempotente. */
    @PutMapping("/{id}/usuarios/{usuarioId}")
    public EntityModel<Usuario> associar(@PathVariable long id, @PathVariable long usuarioId) {
        return usuarios.toModel(servico.associar(id, usuarioId));
    }

    @DeleteMapping("/{id}/usuarios/{usuarioId}")
    public ResponseEntity<Void> desassociar(@PathVariable long id, @PathVariable long usuarioId) {
        servico.desassociar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
