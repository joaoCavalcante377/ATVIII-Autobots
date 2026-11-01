package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.EnderecoRequisicao;
import com.autobots.automanager.servicos.EnderecoServico;

@RestController
@RequestMapping("/enderecos")
public class EnderecoControle {
    private final EnderecoServico servico;
    private final EnderecoRepresentacao representacao;

    public EnderecoControle(EnderecoServico servico, EnderecoRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<Endereco>> listar(@RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long empresaId) {
        var registros = servico.listar(usuarioId, empresaId);
        var proprio = linkTo(methodOn(EnderecoControle.class).listar(usuarioId, empresaId)).withSelfRel().expand();
        if (usuarioId != null) {
            return Colecoes.de(registros, representacao, Endereco.class,
                    registros.isEmpty() ? proprio.andAffordance(afford(methodOn(EnderecoControle.class).criarEndereco(usuarioId, null, null))) : proprio,
                    Ligacoes.usuario(usuarioId, "usuario"));
        }
        if (empresaId != null) {
            return Colecoes.de(registros, representacao, Endereco.class,
                    registros.isEmpty() ? proprio.andAffordance(afford(methodOn(EnderecoControle.class).criarEndereco(null, empresaId, null))) : proprio,
                    Ligacoes.empresa(empresaId, "empresa"));
        }
        return Colecoes.de(registros, representacao, Endereco.class, proprio);
    }

    @GetMapping("/{id}")
    public EntityModel<Endereco> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    /** Cadastra o endereco de um usuario (usuarioId) ou de uma empresa (empresaId); cada um possui no maximo um. */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<Endereco>> criarEndereco(@RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long empresaId, @Valid @RequestBody EnderecoRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(usuarioId, empresaId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<Endereco> atualizarEndereco(@PathVariable long id, @Valid @RequestBody EnderecoRequisicao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirEndereco(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
