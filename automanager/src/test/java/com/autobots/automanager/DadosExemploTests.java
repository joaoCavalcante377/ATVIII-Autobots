package com.autobots.automanager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.autobots.automanager.configuracao.DadosExemplo;
import com.autobots.automanager.repositorios.*;

/** Os dados de exemplo do enunciado sao cadastrados pelas mesmas regras da API e apenas uma vez. */
@SpringBootTest(properties = { "automanager.dados-exemplo=true", "spring.datasource.url=jdbc:h2:mem:autobots-exemplo;DB_CLOSE_DELAY=-1" })
class DadosExemploTests {
    @Autowired
    private DadosExemplo dados;
    @Autowired
    private EmpresaRepositorio empresas;
    @Autowired
    private UsuarioRepositorio usuarios;
    @Autowired
    private VendaRepositorio vendas;
    @Autowired
    private CredencialRepositorio credenciais;

    @Test
    void cadastraOExemploUmaUnicaVez() {
        assertThat(empresas.count()).isEqualTo(2);
        assertThat(usuarios.count()).isEqualTo(3);
        assertThat(credenciais.count()).isEqualTo(4);
        assertThat(vendas.findAll()).extracting(v -> v.valorTotal().intValue()).containsExactlyInAnyOrder(400, 380);
        dados.run();
        assertThat(empresas.count()).isEqualTo(2);
        assertThat(vendas.count()).isEqualTo(2);
    }
}
