package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.TelefoneRequisicao;
import com.autobots.automanager.servicos.TelefoneServico;

@RestController
@RequestMapping("/telefones")
public class TelefoneControle {
    private final TelefoneServico servico;
    private final TelefoneRepresentacao representacao;

    public TelefoneControle(TelefoneServico servico, TelefoneRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<Telefone>> listar(@RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long empresaId) {
        var registros = servico.listar(usuarioId, empresaId);
        var proprio = linkTo(methodOn(TelefoneControle.class).listar(usuarioId, empresaId)).withSelfRel().expand();
        if (usuarioId != null) {
            return Colecoes.de(registros, representacao, Telefone.class,
                    proprio.andAffordance(afford(methodOn(TelefoneControle.class).criarTelefone(usuarioId, null, null))),
                    Ligacoes.usuario(usuarioId, "usuario"));
        }
        if (empresaId != null) {
            return Colecoes.de(registros, representacao, Telefone.class,
                    proprio.andAffordance(afford(methodOn(TelefoneControle.class).criarTelefone(null, empresaId, null))),
                    Ligacoes.empresa(empresaId, "empresa"));
        }
        return Colecoes.de(registros, representacao, Telefone.class, proprio);
    }

    @GetMapping("/{id}")
    public EntityModel<Telefone> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    /** Cadastra o telefone de um usuario (usuarioId) ou de uma empresa (empresaId). */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<Telefone>> criarTelefone(@RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long empresaId, @Valid @RequestBody TelefoneRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(usuarioId, empresaId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<Telefone> atualizarTelefone(@PathVariable long id, @Valid @RequestBody TelefoneRequisicao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirTelefone(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
