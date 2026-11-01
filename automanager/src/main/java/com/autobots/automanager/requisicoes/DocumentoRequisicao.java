package com.autobots.automanager.requisicoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import com.autobots.automanager.enumeracoes.TipoDocumento;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoRequisicao {
    private @NotNull TipoDocumento tipo;
    private @NotBlank String numero;
    private @NotNull @PastOrPresent LocalDate dataEmissao;
}

