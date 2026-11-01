package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.representacoes.*;
import com.autobots.automanager.requisicoes.DocumentoRequisicao;
import com.autobots.automanager.servicos.DocumentoServico;

@RestController
@RequestMapping("/documentos")
public class DocumentoControle {
    private final DocumentoServico servico;
    private final DocumentoRepresentacao representacao;

    public DocumentoControle(DocumentoServico servico, DocumentoRepresentacao representacao) {
        this.servico = servico;
        this.representacao = representacao;
    }

    @GetMapping
    public CollectionModel<EntityModel<Documento>> listar(@RequestParam(required = false) Long usuarioId) {
        var registros = servico.listar(usuarioId);
        var proprio = linkTo(methodOn(DocumentoControle.class).listar(usuarioId)).withSelfRel().expand();
        if (usuarioId == null) return Colecoes.de(registros, representacao, Documento.class, proprio);
        return Colecoes.de(registros, representacao, Documento.class,
                proprio.andAffordance(afford(methodOn(DocumentoControle.class).criarDocumento(usuarioId, null))), Ligacoes.usuario(usuarioId, "usuario"));
    }

    @GetMapping("/{id}")
    public EntityModel<Documento> buscar(@PathVariable long id) {
        return representacao.toModel(servico.buscar(id));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<EntityModel<Documento>> criarDocumento(@RequestParam long usuarioId, @Valid @RequestBody DocumentoRequisicao dados) {
        var modelo = representacao.toModel(servico.criar(usuarioId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EntityModel<Documento> atualizarDocumento(@PathVariable long id, @Valid @RequestBody DocumentoRequisicao dados) {
        return representacao.toModel(servico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirDocumento(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
