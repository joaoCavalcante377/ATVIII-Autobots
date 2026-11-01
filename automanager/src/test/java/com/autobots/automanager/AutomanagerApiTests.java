package com.autobots.automanager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.jayway.jsonpath.JsonPath;

/** Testes de ponta a ponta da API por HTTP simulado, com banco H2 em memoria recriado a cada teste. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AutomanagerApiTests {
    private static final MediaType HAL = MediaType.parseMediaType("application/hal+json");
    private static final MediaType HAL_FORMS = MediaType.parseMediaType("application/prs.hal-forms+json");

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PasswordEncoder codificador;

    private long empresa;
    private long outraEmpresa;
    private long cliente;
    private long funcionario;
    private long fornecedor;

    private ResultActions enviar(String metodo, String url, String json) throws Exception {
        var requisicao = request(org.springframework.http.HttpMethod.valueOf(metodo), url).accept(HAL);
        if (json != null) requisicao.contentType(MediaType.APPLICATION_JSON).content(json);
        return mvc.perform(requisicao);
    }

    private long criar(String url, String json) throws Exception {
        String corpo = enviar("POST", url, json).andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/"))).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    private long usuario(String nome, String perfis, Long empresaId) throws Exception {
        String url = empresaId == null ? "/usuarios" : "/usuarios?empresaId=" + empresaId;
        return criar(url, "{\"nome\":\"" + nome + "\",\"perfis\":[" + perfis + "]}");
    }

    @BeforeEach
    void preparar() throws Exception {
        empresa = criar("/empresas", """
                {"razaoSocial":"Toyota servicos ltda","nomeFantasia":"Toyota Center",
                 "endereco":{"estado":"SP","cidade":"Sao Paulo","bairro":"Centro","rua":"Av. Sao Joao","numero":"10","codigoPostal":"01035-000"},
                 "telefones":[{"ddd":"11","numero":"986454527"}]}""");
        outraEmpresa = criar("/empresas", "{\"razaoSocial\":\"Volkswagen pecas ltda\"}");
        cliente = usuario("Ana Cliente", "\"CLIENTE\"", empresa);
        funcionario = usuario("Bruno Funcionario", "\"FUNCIONARIO\"", empresa);
        fornecedor = usuario("Carla Fornecedora", "\"FORNECEDOR\"", empresa);
    }

    @Test
    void raizApontaParaTodasAsColecoes() throws Exception {
        enviar("GET", "/", null).andExpect(status().isOk())
                .andExpect(jsonPath("$._links.empresas.href").value(endsWith("/empresas")))
                .andExpect(jsonPath("$._links.usuarios.href").exists())
                .andExpect(jsonPath("$._links.veiculos.href").exists())
                .andExpect(jsonPath("$._links.mercadorias.href").exists())
                .andExpect(jsonPath("$._links.servicos.href").exists())
                .andExpect(jsonPath("$._links.vendas.href").exists())
                .andExpect(jsonPath("$._links.credenciais.href").exists())
                .andExpect(jsonPath("$._links.emails.href").exists());
    }

    @Test
    void crudDeEmpresaComLinksEFormularios() throws Exception {
        enviar("GET", "/empresas/" + empresa, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Toyota servicos ltda"))
                .andExpect(jsonPath("$.cadastro").exists())
                .andExpect(jsonPath("$.endereco.cidade").value("Sao Paulo"))
                .andExpect(jsonPath("$.telefones[0].numero").value("986454527"))
                .andExpect(jsonPath("$.usuarios").doesNotExist())
                .andExpect(jsonPath("$._links.usuarios.href").value(endsWith("/usuarios?empresaId=" + empresa)))
                .andExpect(jsonPath("$._links.mercadorias.href").value(endsWith("/mercadorias?empresaId=" + empresa)))
                .andExpect(jsonPath("$._links.servicos.href").exists())
                .andExpect(jsonPath("$._links.vendas.href").exists())
                .andExpect(jsonPath("$._links.endereco.href").exists());

        mvc.perform(get("/empresas/" + empresa).accept(HAL_FORMS)).andExpect(status().isOk())
                .andExpect(jsonPath("$._templates.default.method").value("PUT"))
                .andExpect(jsonPath("$._templates.default.properties[?(@.name=='razaoSocial')].readOnly").value(everyItem(nullValue())))
                .andExpect(jsonPath("$._templates.excluirEmpresa.method").value("DELETE"))
                .andExpect(jsonPath("$._templates.criarUsuario.target").value(endsWith("/usuarios?empresaId=" + empresa)))
                .andExpect(jsonPath("$._templates.criarMercadoria.target").value(endsWith("/mercadorias?empresaId=" + empresa)))
                .andExpect(jsonPath("$._templates.criarServico.method").value("POST"))
                .andExpect(jsonPath("$._templates.criarVenda.method").value("POST"))
                .andExpect(jsonPath("$._templates.criarTelefone.method").value("POST"));

        enviar("PUT", "/empresas/" + empresa, "{\"razaoSocial\":\"Toyota Brasil ltda\",\"nomeFantasia\":\"Toyota\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.razaoSocial").value("Toyota Brasil ltda"));
        enviar("GET", "/empresas", null).andExpect(jsonPath("$._embedded.empresas", hasSize(2)));
        enviar("POST", "/empresas", "{\"razaoSocial\":\" \"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0]").value(startsWith("razaoSocial")));
        enviar("POST", "/empresas", "{\"id\":5,\"razaoSocial\":\"X\"}").andExpect(status().isBadRequest());
        enviar("DELETE", "/empresas/" + outraEmpresa, null).andExpect(status().isNoContent());
        enviar("GET", "/empresas/" + outraEmpresa, null).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Empresa nao encontrada"));
    }

    @Test
    void usuariosPorPerfilEAssociacaoComEmpresas() throws Exception {
        enviar("GET", "/usuarios?perfil=CLIENTE", null).andExpect(jsonPath("$._embedded.usuarios[*].id", contains((int) cliente)));
        enviar("GET", "/usuarios?empresaId=" + empresa, null).andExpect(jsonPath("$._embedded.usuarios", hasSize(3)));
        enviar("GET", "/usuarios?empresaId=" + outraEmpresa, null).andExpect(jsonPath("$._embedded.usuarios").doesNotExist());

        enviar("PUT", "/empresas/" + outraEmpresa + "/usuarios/" + cliente, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cliente));
        enviar("PUT", "/empresas/" + outraEmpresa + "/usuarios/" + cliente, null).andExpect(status().isOk());
        enviar("GET", "/empresas?usuarioId=" + cliente, null).andExpect(jsonPath("$._embedded.empresas", hasSize(2)));
        enviar("GET", "/usuarios?perfil=CLIENTE&empresaId=" + outraEmpresa, null)
                .andExpect(jsonPath("$._embedded.usuarios[*].id", contains((int) cliente)));
        enviar("DELETE", "/empresas/" + outraEmpresa + "/usuarios/" + cliente, null).andExpect(status().isNoContent());
        enviar("DELETE", "/empresas/" + outraEmpresa + "/usuarios/" + cliente, null).andExpect(status().isNotFound());

        enviar("GET", "/usuarios/" + cliente, null).andExpect(jsonPath("$.perfis", contains("CLIENTE")))
                .andExpect(jsonPath("$._links.veiculos.href").exists())
                .andExpect(jsonPath("$._links.compras.href").value(endsWith("/vendas?clienteId=" + cliente)))
                .andExpect(jsonPath("$._links.vendasRealizadas").doesNotExist())
                .andExpect(jsonPath("$.credenciais").doesNotExist());
        enviar("GET", "/usuarios/" + funcionario, null).andExpect(jsonPath("$._links.vendasRealizadas.href").exists())
                .andExpect(jsonPath("$._links.veiculos").doesNotExist());
        enviar("GET", "/usuarios/" + fornecedor, null).andExpect(jsonPath("$._links.mercadoriasFornecidas.href").exists());

        enviar("POST", "/usuarios", "{\"nome\":\"Sem perfil\",\"perfis\":[]}").andExpect(status().isBadRequest());
        enviar("POST", "/usuarios", "{\"nome\":\"X\",\"perfis\":[\"GERENTE\"]}").andExpect(status().isBadRequest());
        enviar("GET", "/usuarios?empresaId=999", null).andExpect(status().isNotFound());
        enviar("PUT", "/usuarios/" + cliente, "{\"nome\":\"Ana Souza\",\"perfis\":[\"CLIENTE\",\"FORNECEDOR\"]}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfis", hasSize(2)));
    }

    @Test
    void cadastroCompletoDeUsuarioEDadosAssociados() throws Exception {
        long id = criar("/usuarios", """
                {"nome":"Dom Pedro","nomeSocial":"Pedro","perfis":["CLIENTE","FUNCIONARIO"],
                 "endereco":{"estado":"SP","cidade":"Santos","bairro":"Centro","rua":"Rua A","numero":"1","codigoPostal":"11000-000"},
                 "telefones":[{"ddd":"13","numero":"999999999"}],
                 "documentos":[{"tipo":"CPF","numero":"12345678900","dataEmissao":"2020-01-10"}],
                 "emails":[{"endereco":"Pedro@Exemplo.com"}]}""");
        enviar("GET", "/usuarios/" + id, null).andExpect(jsonPath("$.emails[0].endereco").value("pedro@exemplo.com"))
                .andExpect(jsonPath("$.documentos[0].tipo").value("CPF"))
                .andExpect(jsonPath("$.endereco.cidade").value("Santos"));

        long telefone = criar("/telefones?usuarioId=" + id, "{\"ddd\":\"11\",\"numero\":\"988887777\"}");
        enviar("GET", "/telefones/" + telefone, null).andExpect(jsonPath("$._links.usuario.href").value(endsWith("/usuarios/" + id)));
        enviar("GET", "/telefones?usuarioId=" + id, null).andExpect(jsonPath("$._embedded.telefones", hasSize(2)));
        enviar("PUT", "/telefones/" + telefone, "{\"ddd\":\"11\",\"numero\":\"977776666\"}").andExpect(jsonPath("$.numero").value("977776666"));
        enviar("POST", "/telefones?usuarioId=" + id + "&empresaId=" + empresa, "{\"ddd\":\"11\",\"numero\":\"988887777\"}")
                .andExpect(status().isBadRequest());
        enviar("POST", "/telefones", "{\"ddd\":\"11\",\"numero\":\"988887777\"}").andExpect(status().isBadRequest());
        enviar("POST", "/telefones?usuarioId=" + id, "{\"ddd\":\"11\",\"numero\":\"12\"}").andExpect(status().isBadRequest());
        long telefoneEmpresa = criar("/telefones?empresaId=" + empresa, "{\"ddd\":\"11\",\"numero\":\"30304040\"}");
        enviar("GET", "/telefones/" + telefoneEmpresa, null).andExpect(jsonPath("$._links.empresa.href").value(endsWith("/empresas/" + empresa)));
        enviar("DELETE", "/telefones/" + telefone, null).andExpect(status().isNoContent());
        enviar("GET", "/telefones?usuarioId=" + id, null).andExpect(jsonPath("$._embedded.telefones", hasSize(1)));

        enviar("POST", "/enderecos?usuarioId=" + id,
                "{\"estado\":\"SP\",\"cidade\":\"X\",\"bairro\":\"Y\",\"rua\":\"Z\",\"numero\":\"1\",\"codigoPostal\":\"0\"}")
                .andExpect(status().isConflict());
        long endereco = criar("/enderecos?usuarioId=" + cliente,
                "{\"estado\":\"SP\",\"cidade\":\"Campinas\",\"bairro\":\"Centro\",\"rua\":\"Rua B\",\"numero\":\"2\",\"codigoPostal\":\"13000-000\"}");
        enviar("GET", "/usuarios/" + cliente, null).andExpect(jsonPath("$._links.endereco.href").value(endsWith("/enderecos/" + endereco)));
        enviar("DELETE", "/enderecos/" + endereco, null).andExpect(status().isNoContent());
        mvc.perform(get("/usuarios/" + cliente).accept(HAL_FORMS))
                .andExpect(jsonPath("$._links.enderecos.href").value(endsWith("/enderecos?usuarioId=" + cliente)))
                .andExpect(jsonPath("$._templates.criarEndereco.method").value("POST"));

        enviar("POST", "/documentos?usuarioId=" + cliente, "{\"tipo\":\"CPF\",\"numero\":\"12345678900\",\"dataEmissao\":\"2020-01-01\"}")
                .andExpect(status().isConflict());
        enviar("POST", "/documentos?usuarioId=" + cliente, "{\"tipo\":\"RG\",\"numero\":\"1\",\"dataEmissao\":\"2999-01-01\"}")
                .andExpect(status().isBadRequest());
        long documento = criar("/documentos?usuarioId=" + cliente, "{\"tipo\":\"CNH\",\"numero\":\"55\",\"dataEmissao\":\"2021-05-05\"}");
        enviar("GET", "/documentos/" + documento, null).andExpect(jsonPath("$._links.usuario.href").value(endsWith("/usuarios/" + cliente)));

        enviar("POST", "/emails?usuarioId=" + cliente, "{\"endereco\":\"invalido\"}").andExpect(status().isBadRequest());
        long email = criar("/emails?usuarioId=" + cliente, "{\"endereco\":\"ana@exemplo.com\"}");
        enviar("PUT", "/emails/" + email, "{\"endereco\":\"ana@loja.com\"}").andExpect(jsonPath("$.endereco").value("ana@loja.com"));
        enviar("DELETE", "/emails/" + email, null).andExpect(status().isNoContent());
        enviar("GET", "/emails/" + email, null).andExpect(status().isNotFound());

        enviar("DELETE", "/usuarios/" + id, null).andExpect(status().isNoContent());
        enviar("GET", "/usuarios/" + id, null).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("select count(*) from documento where numero = '12345678900'", Integer.class)).isZero();
    }

    @Test
    void credenciaisNuncaExpoemASenha() throws Exception {
        String corpo = enviar("POST", "/credenciais?usuarioId=" + funcionario,
                "{\"tipo\":\"USUARIO_SENHA\",\"nomeUsuario\":\"Bruno.Loja\",\"senha\":\"segredo123\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeUsuario").value("bruno.loja"))
                .andExpect(jsonPath("$.tipo").value("USUARIO_SENHA"))
                .andExpect(jsonPath("$.inativo").value(false))
                .andExpect(jsonPath("$.criacao").exists())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$._links.usuario.href").value(endsWith("/usuarios/" + funcionario)))
                .andReturn().getResponse().getContentAsString();
        assertThat(corpo).doesNotContain("segredo123");
        long id = ((Number) JsonPath.read(corpo, "$.id")).longValue();
        String hash = jdbc.queryForObject("select senha from credencial_usuario_senha where id = ?", String.class, id);
        assertThat(hash).isNotEqualTo("segredo123").startsWith("$2");
        assertThat(codificador.matches("segredo123", hash)).isTrue();

        enviar("POST", "/credenciais?usuarioId=" + cliente,
                "{\"tipo\":\"USUARIO_SENHA\",\"nomeUsuario\":\"bruno.loja\",\"senha\":\"outra123\"}").andExpect(status().isConflict());
        enviar("POST", "/credenciais?usuarioId=" + cliente, "{\"tipo\":\"USUARIO_SENHA\",\"nomeUsuario\":\"ana\"}")
                .andExpect(status().isBadRequest());
        enviar("POST", "/credenciais?usuarioId=" + cliente, "{\"tipo\":\"USUARIO_SENHA\",\"nomeUsuario\":\"ana\",\"senha\":\"123\"}")
                .andExpect(status().isBadRequest());
        long cracha = criar("/credenciais?usuarioId=" + funcionario, "{\"tipo\":\"CODIGO_BARRA\",\"codigo\":7891234567895}");
        enviar("POST", "/credenciais?usuarioId=" + cliente, "{\"tipo\":\"CODIGO_BARRA\",\"codigo\":7891234567895}")
                .andExpect(status().isConflict());
        enviar("GET", "/credenciais?usuarioId=" + funcionario, null).andExpect(jsonPath("$._embedded.credenciais", hasSize(2)));

        enviar("PUT", "/credenciais/" + id, "{\"tipo\":\"USUARIO_SENHA\",\"nomeUsuario\":\"bruno.loja\",\"inativo\":true}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.inativo").value(true));
        assertThat(jdbc.queryForObject("select senha from credencial_usuario_senha where id = ?", String.class, id)).isEqualTo(hash);
        enviar("PUT", "/credenciais/" + id, "{\"tipo\":\"CODIGO_BARRA\",\"codigo\":1}").andExpect(status().isBadRequest());
        enviar("DELETE", "/credenciais/" + cracha, null).andExpect(status().isNoContent());
        enviar("GET", "/credenciais?usuarioId=" + funcionario, null).andExpect(jsonPath("$._embedded.credenciais", hasSize(1)));
    }

    @Test
    void veiculosMercadoriasEServicos() throws Exception {
        enviar("POST", "/veiculos?proprietarioId=" + funcionario, "{\"tipo\":\"SUV\",\"modelo\":\"Corolla Cross\",\"placa\":\"ABC1D23\"}")
                .andExpect(status().isBadRequest());
        long veiculo = criar("/veiculos?proprietarioId=" + cliente, "{\"tipo\":\"SUV\",\"modelo\":\"Corolla Cross\",\"placa\":\"abc1d23\"}");
        enviar("GET", "/veiculos/" + veiculo, null).andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$._links.proprietario.href").value(endsWith("/usuarios/" + cliente)))
                .andExpect(jsonPath("$._links.vendas.href").value(endsWith("/vendas?veiculoId=" + veiculo)));
        enviar("POST", "/veiculos?proprietarioId=" + cliente, "{\"tipo\":\"HATCH\",\"modelo\":\"Etios\",\"placa\":\"ABC1D23\"}")
                .andExpect(status().isConflict());
        enviar("POST", "/veiculos?proprietarioId=" + cliente, "{\"tipo\":\"HATCH\",\"modelo\":\"Etios\",\"placa\":\"12345\"}")
                .andExpect(status().isBadRequest());
        enviar("PUT", "/veiculos/" + veiculo, "{\"tipo\":\"SUV\",\"modelo\":\"Corolla Cross XRE\",\"placa\":\"ABC-1234\"}")
                .andExpect(jsonPath("$.placa").value("ABC-1234"));
        enviar("GET", "/veiculos?proprietarioId=" + cliente, null).andExpect(jsonPath("$._embedded.veiculos", hasSize(1)));

        long peca = criar("/mercadorias?empresaId=" + empresa, """
                {"nome":"Pastilha de freio","quantidade":20,"valor":120.50,"fabricacao":"2026-01-01","validade":"2030-01-01",
                 "fornecedorId":%d}""".formatted(fornecedor));
        enviar("GET", "/mercadorias/" + peca, null).andExpect(jsonPath("$.valor").value(120.5))
                .andExpect(jsonPath("$.cadastro").exists())
                .andExpect(jsonPath("$._links.empresa.href").value(endsWith("/empresas/" + empresa)))
                .andExpect(jsonPath("$._links.fornecedor.href").value(endsWith("/usuarios/" + fornecedor)));
        enviar("GET", "/mercadorias?fornecedorId=" + fornecedor, null).andExpect(jsonPath("$._embedded.mercadorias", hasSize(1)));
        enviar("POST", "/mercadorias?empresaId=" + empresa, "{\"nome\":\"X\",\"quantidade\":1,\"valor\":1,\"fornecedorId\":" + cliente + "}")
                .andExpect(status().isBadRequest());
        enviar("POST", "/mercadorias?empresaId=" + outraEmpresa, "{\"nome\":\"X\",\"quantidade\":1,\"valor\":1,\"fornecedorId\":" + fornecedor + "}")
                .andExpect(status().isBadRequest());
        enviar("POST", "/mercadorias?empresaId=" + empresa,
                "{\"nome\":\"X\",\"quantidade\":1,\"valor\":1,\"fabricacao\":\"2026-01-02\",\"validade\":\"2026-01-01\"}")
                .andExpect(status().isBadRequest());
        enviar("POST", "/mercadorias?empresaId=" + empresa, "{\"nome\":\"X\",\"quantidade\":-1,\"valor\":-1}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos", hasSize(2)));
        enviar("PUT", "/mercadorias/" + peca, "{\"nome\":\"Pastilha de freio dianteira\",\"quantidade\":18,\"valor\":130}")
                .andExpect(jsonPath("$.quantidade").value(18)).andExpect(jsonPath("$._links.fornecedor").doesNotExist());

        long servico = criar("/servicos?empresaId=" + empresa, "{\"nome\":\"Troca de pastilhas\",\"valor\":80}");
        enviar("GET", "/servicos?empresaId=" + empresa, null).andExpect(jsonPath("$._embedded.servicos[0].id").value(servico));
        enviar("GET", "/servicos?empresaId=" + outraEmpresa, null).andExpect(jsonPath("$._embedded.servicos").doesNotExist());
        enviar("PUT", "/servicos/" + servico, "{\"nome\":\"Troca de pastilhas\",\"valor\":90,\"descricao\":\"Inclui mao de obra\"}")
                .andExpect(jsonPath("$.descricao").value("Inclui mao de obra"));
        enviar("DELETE", "/servicos/" + servico, null).andExpect(status().isNoContent());
        enviar("DELETE", "/mercadorias/" + peca, null).andExpect(status().isNoContent());
        enviar("DELETE", "/veiculos/" + veiculo, null).andExpect(status().isNoContent());
    }

    @Test
    void vendaDeMercadoriasEServicosComRegrasDeConsistencia() throws Exception {
        long veiculo = criar("/veiculos?proprietarioId=" + cliente, "{\"tipo\":\"HATCH\",\"modelo\":\"Etios\",\"placa\":\"ABC-0000\"}");
        long roda = criar("/mercadorias?empresaId=" + empresa,
                "{\"nome\":\"Roda liga leve\",\"quantidade\":30,\"valor\":300,\"fornecedorId\":" + fornecedor + "}");
        long alinhamento = criar("/servicos?empresaId=" + empresa, "{\"nome\":\"Alinhamento\",\"valor\":50}");
        long balanceamento = criar("/servicos?empresaId=" + empresa, "{\"nome\":\"Balanceamento\",\"valor\":30.25}");
        long servicoVw = criar("/servicos?empresaId=" + outraEmpresa, "{\"nome\":\"Revisao\",\"valor\":450}");

        String venda = """
                {"identificacao":"%s","clienteId":%d,"funcionarioId":%d,"veiculoId":%s,"mercadoriaIds":[%s],"servicoIds":[%s]}""";
        long id = criar("/vendas?empresaId=" + empresa, venda.formatted("V-001", cliente, funcionario, veiculo, roda,
                alinhamento + "," + balanceamento));
        enviar("GET", "/vendas/" + id, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("V-001"))
                .andExpect(jsonPath("$.valorTotal").value(380.25))
                .andExpect(jsonPath("$.cliente.nome").value("Ana Cliente"))
                .andExpect(jsonPath("$.funcionario.id").value(funcionario))
                .andExpect(jsonPath("$.veiculo.placa").value("ABC-0000"))
                .andExpect(jsonPath("$.mercadorias", hasSize(1)))
                .andExpect(jsonPath("$.servicos[*].nome", contains("Alinhamento", "Balanceamento")))
                .andExpect(jsonPath("$._links.empresa.href").value(endsWith("/empresas/" + empresa)))
                .andExpect(jsonPath("$._links.cliente.href").value(endsWith("/usuarios/" + cliente)))
                .andExpect(jsonPath("$._links.funcionario.href").value(endsWith("/usuarios/" + funcionario)))
                .andExpect(jsonPath("$._links.veiculo.href").value(endsWith("/veiculos/" + veiculo)))
                .andExpect(jsonPath("$._links.servico", hasSize(2)));
        long semVeiculo = criar("/vendas?empresaId=" + empresa,
                "{\"clienteId\":%d,\"funcionarioId\":%d,\"servicoIds\":[%d]}".formatted(cliente, funcionario, alinhamento));
        enviar("GET", "/vendas/" + semVeiculo, null).andExpect(jsonPath("$.identificacao").value(matchesPattern("[0-9A-F]{12}")))
                .andExpect(jsonPath("$.veiculo").doesNotExist());

        enviar("GET", "/vendas?clienteId=" + cliente, null).andExpect(jsonPath("$._embedded.vendas", hasSize(2)));
        enviar("GET", "/vendas?veiculoId=" + veiculo, null).andExpect(jsonPath("$._embedded.vendas", hasSize(1)));
        enviar("GET", "/vendas?empresaId=" + outraEmpresa, null).andExpect(jsonPath("$._embedded.vendas").doesNotExist());
        mvc.perform(get("/vendas?empresaId=" + empresa).accept(HAL_FORMS))
                .andExpect(jsonPath("$._templates.default.method").value("POST"))
                .andExpect(jsonPath("$._templates.default.properties[*].name", hasItems("clienteId", "funcionarioId", "mercadoriaIds")));

        String base = "/vendas?empresaId=" + empresa;
        enviar("POST", base, venda.formatted("V-001", cliente, funcionario, "null", roda, "")).andExpect(status().isConflict());
        enviar("POST", base, venda.formatted("V-002", cliente, funcionario, "null", "", "")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("pelo menos uma")));
        enviar("POST", base, venda.formatted("V-003", funcionario, funcionario, "null", roda, "")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("perfil CLIENTE")));
        enviar("POST", base, venda.formatted("V-004", cliente, fornecedor, "null", roda, "")).andExpect(status().isBadRequest());
        enviar("POST", base, venda.formatted("V-005", cliente, funcionario, "null", "", servicoVw)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("nao pertence a empresa")));
        long outroCliente = usuario("Outro cliente", "\"CLIENTE\"", empresa);
        enviar("POST", base, venda.formatted("V-006", outroCliente, funcionario, veiculo, roda, "")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("nao pertence ao cliente")));
        long clienteSemVinculo = usuario("Cliente VW", "\"CLIENTE\"", outraEmpresa);
        enviar("POST", base, venda.formatted("V-007", clienteSemVinculo, funcionario, "null", roda, "")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("nao esta associado")));
        enviar("POST", base, venda.formatted("V-008", cliente, funcionario, "null", 999, "")).andExpect(status().isNotFound());

        enviar("DELETE", "/mercadorias/" + roda, null).andExpect(status().isConflict());
        enviar("DELETE", "/servicos/" + alinhamento, null).andExpect(status().isConflict());
        enviar("DELETE", "/veiculos/" + veiculo, null).andExpect(status().isConflict());
        enviar("DELETE", "/usuarios/" + cliente, null).andExpect(status().isConflict());
        enviar("DELETE", "/usuarios/" + fornecedor, null).andExpect(status().isConflict());
        enviar("DELETE", "/empresas/" + empresa + "/usuarios/" + funcionario, null).andExpect(status().isConflict());
        enviar("PUT", "/usuarios/" + funcionario, "{\"nome\":\"Bruno\",\"perfis\":[\"CLIENTE\"]}").andExpect(status().isConflict());
        enviar("PUT", "/usuarios/" + fornecedor, "{\"nome\":\"Carla\",\"perfis\":[\"CLIENTE\"]}").andExpect(status().isConflict());

        enviar("PUT", "/vendas/" + id, venda.formatted("V-001", cliente, funcionario, "null", "", alinhamento))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorTotal").value(50.0))
                .andExpect(jsonPath("$.veiculo").doesNotExist());
        enviar("DELETE", "/vendas/" + id, null).andExpect(status().isNoContent());
        enviar("DELETE", "/mercadorias/" + roda, null).andExpect(status().isNoContent());
        enviar("GET", "/vendas/" + id, null).andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value("Venda nao encontrada"));
    }

    @Test
    void excluirEmpresaRemoveSeusRegistrosEMantemUsuarios() throws Exception {
        long roda = criar("/mercadorias?empresaId=" + empresa, "{\"nome\":\"Roda\",\"quantidade\":1,\"valor\":300}");
        long servico = criar("/servicos?empresaId=" + empresa, "{\"nome\":\"Alinhamento\",\"valor\":50}");
        criar("/vendas?empresaId=" + empresa, "{\"clienteId\":%d,\"funcionarioId\":%d,\"mercadoriaIds\":[%d],\"servicoIds\":[%d]}"
                .formatted(cliente, funcionario, roda, servico));
        enviar("DELETE", "/empresas/" + empresa, null).andExpect(status().isNoContent());
        enviar("GET", "/mercadorias/" + roda, null).andExpect(status().isNotFound());
        enviar("GET", "/servicos/" + servico, null).andExpect(status().isNotFound());
        enviar("GET", "/vendas", null).andExpect(jsonPath("$._embedded.vendas").doesNotExist());
        enviar("GET", "/usuarios/" + cliente, null).andExpect(status().isOk());
        enviar("GET", "/empresas?usuarioId=" + cliente, null).andExpect(jsonPath("$._embedded.empresas").doesNotExist());
        enviar("DELETE", "/usuarios/" + cliente, null).andExpect(status().isNoContent());
    }

    @Test
    void errosSeguemProblemDetail() throws Exception {
        enviar("GET", "/usuarios/999", null).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Usuario nao encontrado"));
        mvc.perform(post("/empresas").contentType(MediaType.TEXT_PLAIN).content("x")).andExpect(status().isUnsupportedMediaType());
        mvc.perform(patch("/empresas/" + empresa)).andExpect(status().isMethodNotAllowed());
        enviar("POST", "/empresas", "{nao e json").andExpect(status().isBadRequest());
        enviar("GET", "/vendas?empresaId=abc", null).andExpect(status().isBadRequest());
    }
}
