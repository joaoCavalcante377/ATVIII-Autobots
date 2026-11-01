package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.VeiculoRequisicao;
import com.autobots.automanager.servicos.VeiculoServico;

@RestController
@RequestMapping("/veiculos")
public class VeiculoControle {
    private final VeiculoServico servico;
    private final VeiculoRepresentacao representacao;

    public VeiculoControle(VeiculoServico servico, VeiculoRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<Veiculo>> listar(@RequestParam(required = false) Long proprietarioId) {
        var registros = servico.listar(proprietarioId);
        var proprio = linkTo(methodOn(VeiculoControle.class).listar(proprietarioId)).withSelfRel().expand();
        if (proprietarioId == null) return Colecoes.de(registros, representacao, Veiculo.class, proprio);
        return Colecoes.de(registros, representacao, Veiculo.class,
                proprio.andAffordance(afford(methodOn(VeiculoControle.class).criarVeiculo(proprietarioId, null))), Ligacoes.usuario(proprietarioId, "proprietario"));
    }

    @GetMapping("/{id}")
    public EntityModel<Veiculo> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<Veiculo>> criarVeiculo(@RequestParam long proprietarioId, @Valid @RequestBody VeiculoRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(proprietarioId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<Veiculo> atualizarVeiculo(@PathVariable long id, @Valid @RequestBody VeiculoRequisicao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirVeiculo(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
