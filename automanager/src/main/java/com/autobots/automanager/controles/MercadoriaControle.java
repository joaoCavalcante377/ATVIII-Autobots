package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import java.util.ArrayList;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.MercadoriaRequisicao;
import com.autobots.automanager.servicos.MercadoriaServico;

@RestController
@RequestMapping("/mercadorias")
public class MercadoriaControle {
    private final MercadoriaServico servico;
    private final MercadoriaRepresentacao representacao;

    public MercadoriaControle(MercadoriaServico servico, MercadoriaRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<Mercadoria>> listar(@RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long fornecedorId) {
        var registros = servico.listar(empresaId, fornecedorId);
        var proprio = linkTo(methodOn(MercadoriaControle.class).listar(empresaId, fornecedorId)).withSelfRel().expand();
        var extras = new ArrayList<Link>();
        if (empresaId != null) {
            proprio = proprio.andAffordance(afford(methodOn(MercadoriaControle.class).criarMercadoria(empresaId, null)));
            extras.add(Ligacoes.empresa(empresaId, "empresa"));
        }
        if (fornecedorId != null) extras.add(Ligacoes.usuario(fornecedorId, "fornecedor"));
        return Colecoes.de(registros, representacao, Mercadoria.class, proprio, extras.toArray(Link[]::new));
    }

    @GetMapping("/{id}")
    public EntityModel<Mercadoria> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<Mercadoria>> criarMercadoria(@RequestParam long empresaId, @Valid @RequestBody MercadoriaRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(empresaId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<Mercadoria> atualizarMercadoria(@PathVariable long id, @Valid @RequestBody MercadoriaRequisicao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirMercadoria(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
