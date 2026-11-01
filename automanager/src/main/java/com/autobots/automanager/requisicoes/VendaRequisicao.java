package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Venda de mercadorias e servicos. Todos os itens e pessoas devem pertencer a empresa da venda.
 * A identificacao e opcional no cadastro: quando omitida, o servidor gera uma.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaRequisicao {
    private @Size(max = 60) String identificacao;
    private @NotNull Long clienteId;
    private @NotNull Long funcionarioId;
    private Long veiculoId;
    private Set<@NotNull Long> mercadoriaIds;
    private Set<@NotNull Long> servicoIds;
}

