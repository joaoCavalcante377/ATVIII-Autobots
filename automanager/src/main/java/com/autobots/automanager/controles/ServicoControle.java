package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.ServicoRequisicao;
import com.autobots.automanager.servicos.ServicoServico;

@RestController
@RequestMapping("/servicos")
public class ServicoControle {
    private final ServicoServico servico;
    private final ServicoRepresentacao representacao;

    public ServicoControle(ServicoServico servico, ServicoRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<Servico>> listar(@RequestParam(required = false) Long empresaId) {
        var registros = servico.listar(empresaId);
        var proprio = linkTo(methodOn(ServicoControle.class).listar(empresaId)).withSelfRel().expand();
        if (empresaId == null) return Colecoes.de(registros, representacao, Servico.class, proprio);
        return Colecoes.de(registros, representacao, Servico.class,
                proprio.andAffordance(afford(methodOn(ServicoControle.class).criarServico(empresaId, null))), Ligacoes.empresa(empresaId, "empresa"));
    }

    @GetMapping("/{id}")
    public EntityModel<Servico> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<Servico>> criarServico(@RequestParam long empresaId, @Valid @RequestBody ServicoRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(empresaId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<Servico> atualizarServico(@PathVariable long id, @Valid @RequestBody ServicoRequisicao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirServico(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
