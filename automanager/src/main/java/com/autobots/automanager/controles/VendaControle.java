package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import java.util.ArrayList;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.VendaRequisicao;
import com.autobots.automanager.servicos.VendaServico;

@RestController
@RequestMapping("/vendas")
public class VendaControle {
    private final VendaServico servico;
    private final VendaRepresentacao representacao;

    public VendaControle(VendaServico servico, VendaRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<VendaResposta>> listar(@RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long clienteId, @RequestParam(required = false) Long funcionarioId,
            @RequestParam(required = false) Long veiculoId) {
        var registros = servico.listar(empresaId, clienteId, funcionarioId, veiculoId);
        var proprio = linkTo(methodOn(VendaControle.class).listar(empresaId, clienteId, funcionarioId, veiculoId)).withSelfRel().expand();
        var extras = new ArrayList<Link>();
        if (empresaId != null) {
            proprio = proprio.andAffordance(afford(methodOn(VendaControle.class).criarVenda(empresaId, null)));
            extras.add(Ligacoes.empresa(empresaId, "empresa"));
        }
        if (clienteId != null) extras.add(Ligacoes.usuario(clienteId, "cliente"));
        if (funcionarioId != null) extras.add(Ligacoes.usuario(funcionarioId, "funcionario"));
        if (veiculoId != null) extras.add(linkTo(methodOn(VeiculoControle.class).buscar(veiculoId)).withRel("veiculo").expand());
        return Colecoes.de(registros, representacao, VendaResposta.class, proprio, extras.toArray(Link[]::new));
    }

    @GetMapping("/{id}")
    public EntityModel<VendaResposta> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    /** Registra uma venda da empresa informada em empresaId. */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<VendaResposta>> criarVenda(@RequestParam long empresaId, @Valid @RequestBody VendaRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(empresaId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<VendaResposta> atualizarVenda(@PathVariable long id, @Valid @RequestBody VendaRequisicao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirVenda(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
