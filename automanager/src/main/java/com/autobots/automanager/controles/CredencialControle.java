package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.CredencialRequisicao;
import com.autobots.automanager.servicos.CredencialServico;

@RestController
@RequestMapping("/credenciais")
public class CredencialControle {
    private final CredencialServico servico;
    private final CredencialRepresentacao representacao;

    public CredencialControle(CredencialServico servico, CredencialRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<CredencialResposta>> listar(@RequestParam(required = false) Long usuarioId) {
        var registros = servico.listar(usuarioId);
        var proprio = linkTo(methodOn(CredencialControle.class).listar(usuarioId)).withSelfRel().expand();
        if (usuarioId == null) return Colecoes.de(registros, representacao, CredencialResposta.class, proprio);
        return Colecoes.de(registros, representacao, CredencialResposta.class,
                proprio.andAffordance(afford(methodOn(CredencialControle.class).criarCredencial(usuarioId, null))), Ligacoes.usuario(usuarioId, "usuario"));
    }

    @GetMapping("/{id}")
    public EntityModel<CredencialResposta> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<CredencialResposta>> criarCredencial(@RequestParam long usuarioId, @Valid @RequestBody CredencialRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(usuarioId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<CredencialResposta> atualizarCredencial(@PathVariable long id, @Valid @RequestBody CredencialRequisicao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCredencial(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
