package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.EmailRequisicao;
import com.autobots.automanager.servicos.EmailServico;

@RestController
@RequestMapping("/emails")
public class EmailControle {
    private final EmailServico servico;
    private final EmailRepresentacao representacao;

    public EmailControle(EmailServico servico, EmailRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<Email>> listar(@RequestParam(required = false) Long usuarioId) {
        var registros = servico.listar(usuarioId);
        var proprio = linkTo(methodOn(EmailControle.class).listar(usuarioId)).withSelfRel().expand();
        if (usuarioId == null) return Colecoes.de(registros, representacao, Email.class, proprio);
        return Colecoes.de(registros, representacao, Email.class,
                proprio.andAffordance(afford(methodOn(EmailControle.class).criarEmail(usuarioId, null))), Ligacoes.usuario(usuarioId, "usuario"));
    }

    @GetMapping("/{id}")
    public EntityModel<Email> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<Email>> criarEmail(@RequestParam long usuarioId, @Valid @RequestBody EmailRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(usuarioId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<Email> atualizarEmail(@PathVariable long id, @Valid @RequestBody EmailRequisicao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirEmail(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
