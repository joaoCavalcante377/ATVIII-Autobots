package com.autobots.automanager.representacoes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.server.core.Relation;
import com.autobots.automanager.entidades.*;

/** Resumo da venda com os itens, as pessoas envolvidas e o valor total calculado. */
@Relation(itemRelation = "venda", collectionRelation = "vendas")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VendaResposta(Long id, String identificacao, Date cadastro, Pessoa cliente, Pessoa funcionario,
        VeiculoVendido veiculo, List<Item> mercadorias, List<Item> servicos, BigDecimal valorTotal) {

    public record Pessoa(Long id, String nome) {
    }

    public record VeiculoVendido(Long id, String placa, String modelo) {
    }

    public record Item(Long id, String nome, BigDecimal valor) {
    }

    public static VendaResposta de(Venda v) {
        var veiculo = v.getVeiculo() == null ? null
                : new VeiculoVendido(v.getVeiculo().getId(), v.getVeiculo().getPlaca(), v.getVeiculo().getModelo());
        return new VendaResposta(v.getId(), v.getIdentificacao(), v.getCadastro(),
                new Pessoa(v.getCliente().getId(), v.getCliente().getNome()),
                new Pessoa(v.getFuncionario().getId(), v.getFuncionario().getNome()), veiculo,
                v.getMercadorias().stream().sorted(Comparator.comparing(Mercadoria::getId))
                        .map(m -> new Item(m.getId(), m.getNome(), m.getValor())).toList(),
                v.getServicos().stream().sorted(Comparator.comparing(Servico::getId))
                        .map(s -> new Item(s.getId(), s.getNome(), s.getValor())).toList(),
                v.valorTotal());
    }
}
