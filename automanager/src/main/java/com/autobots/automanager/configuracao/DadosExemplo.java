package com.autobots.automanager.configuracao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import com.autobots.automanager.enumeracoes.*;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.requisicoes.*;
import com.autobots.automanager.servicos.*;

/**
 * Cadastra os dados de exemplo do enunciado (adaptados) quando o banco esta vazio.
 * Desative com automanager.dados-exemplo=false.
 */
@Component
@ConditionalOnProperty(name = "automanager.dados-exemplo", havingValue = "true")
public class DadosExemplo implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(DadosExemplo.class);
    private final EmpresaRepositorio repositorio;
    private final EmpresaServico empresas;
    private final UsuarioServico usuarios;
    private final CredencialServico credenciais;
    private final VeiculoServico veiculos;
    private final MercadoriaServico mercadorias;
    private final ServicoServico servicos;
    private final VendaServico vendas;

    public DadosExemplo(EmpresaRepositorio repositorio, EmpresaServico empresas, UsuarioServico usuarios,
            CredencialServico credenciais, VeiculoServico veiculos, MercadoriaServico mercadorias, ServicoServico servicos,
            VendaServico vendas) {
        this.repositorio = repositorio;
        this.empresas = empresas;
        this.usuarios = usuarios;
        this.credenciais = credenciais;
        this.veiculos = veiculos;
        this.mercadorias = mercadorias;
        this.servicos = servicos;
        this.vendas = vendas;
    }

    @Override
    public void run(String... args) {
        if (repositorio.count() > 0) return;
        LocalDate hoje = LocalDate.now();
        var toyota = empresas.criar(new EmpresaRequisicao("Car service toyota ltda", "Car service manutencao veicular",
                new EnderecoRequisicao("Sao Paulo", "Sao Paulo", "Centro", "Av. Sao Joao", "00", "01035-000", null),
                List.of(new TelefoneRequisicao("011", "986454527"))));
        var volkswagen = empresas.criar(new EmpresaRequisicao("Auto center volkswagen ltda", "Auto center VW",
                new EnderecoRequisicao("Sao Paulo", "Sao Bernardo do Campo", "Centro", "Av. Pereira Barreto", "00", "09751-000", null),
                List.of(new TelefoneRequisicao("011", "943218765"))));

        var funcionario = usuarios.criar(new UsuarioRequisicao("Pedro Alcantara de Braganca e Bourbon", "Dom Pedro",
                Set.of(PerfilUsuario.FUNCIONARIO),
                new EnderecoRequisicao("Sao Paulo", "Sao Paulo", "Jardins", "Av. Sao Gabriel", "00", "01435-001", null),
                List.of(new TelefoneRequisicao("011", "9854633728")),
                List.of(new DocumentoRequisicao(TipoDocumento.CPF, "856473819229", hoje)),
                List.of(new EmailRequisicao("a@a.com"))), toyota.getId());
        credenciais.criar(funcionario.getId(), new CredencialRequisicao(TipoCredencial.USUARIO_SENHA, "dompedrofuncionario", "123456", null, false));
        credenciais.criar(funcionario.getId(), new CredencialRequisicao(TipoCredencial.CODIGO_BARRA, null, null, 7891234567895L, false));

        var fornecedor = usuarios.criar(new UsuarioRequisicao("Componentes varejo de partes automotivas ltda",
                "Loja do carro, vendas de componentes automotivos", Set.of(PerfilUsuario.FORNECEDOR),
                new EnderecoRequisicao("Rio de Janeiro", "Rio de Janeiro", "Centro", "Av. Republica do Chile", "00", "20031-170", null),
                List.of(), List.of(new DocumentoRequisicao(TipoDocumento.CNPJ, "00014556000100", hoje)),
                List.of(new EmailRequisicao("f@f.com"))), toyota.getId());
        credenciais.criar(fornecedor.getId(), new CredencialRequisicao(TipoCredencial.USUARIO_SENHA, "dompedrofornecedor", "123456", null, false));

        var cliente = usuarios.criar(new UsuarioRequisicao("Pedro Alcantara de Braganca e Bourbon", "Dom Pedro cliente",
                Set.of(PerfilUsuario.CLIENTE),
                new EnderecoRequisicao("Sao Paulo", "Sao Jose dos Campos", "Centro", "Av. Dr. Nelson D'Avila", "00", "12245-070", null),
                List.of(), List.of(new DocumentoRequisicao(TipoDocumento.CPF, "12584698533", hoje)),
                List.of(new EmailRequisicao("c@c.com"))), toyota.getId());
        credenciais.criar(cliente.getId(), new CredencialRequisicao(TipoCredencial.USUARIO_SENHA, "dompedrocliente", "123456", null, false));
        empresas.associar(volkswagen.getId(), cliente.getId());

        var veiculo = veiculos.criar(cliente.getId(), new VeiculoRequisicao(TipoVeiculo.SUV, "corolla-cross", "ABC-0000"));
        var roda = mercadorias.criar(toyota.getId(), new MercadoriaRequisicao("Roda de liga leve modelo toyota etios",
                "Roda de liga leve original de fabrica da toyota para modelos do tipo hatch", 30L, new BigDecimal("300.00"),
                hoje.minusMonths(2), hoje.plusYears(5), fornecedor.getId()));
        var troca = servicos.criar(toyota.getId(), new ServicoRequisicao("Troca de rodas", "Troca das rodas do carro por novas", new BigDecimal("50.00")));
        var alinhamento = servicos.criar(toyota.getId(), new ServicoRequisicao("Alinhamento de rodas", "Alinhamento das rodas do carro", new BigDecimal("50.00")));
        var balanceamento = servicos.criar(toyota.getId(), new ServicoRequisicao("Balanceamento de rodas", "Balanceamento das rodas do carro", new BigDecimal("30.00")));
        servicos.criar(volkswagen.getId(), new ServicoRequisicao("Revisao de 10.000 km", "Troca de oleo, filtros e inspecao", new BigDecimal("450.00")));

        vendas.criar(toyota.getId(), new VendaRequisicao("1234698745", cliente.getId(), funcionario.getId(), veiculo.getId(),
                Set.of(roda.getId()), Set.of(troca.getId(), alinhamento.getId())));
        vendas.criar(toyota.getId(), new VendaRequisicao("1234698749", cliente.getId(), funcionario.getId(), veiculo.getId(),
                Set.of(roda.getId()), Set.of(balanceamento.getId(), alinhamento.getId())));
        LOG.info("Dados de exemplo cadastrados: 2 empresas, 3 usuarios, 1 veiculo, 1 mercadoria, 4 servicos e 2 vendas");
    }
}
