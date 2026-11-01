# ATVIII - AutoBots

Continuação da ATVII com a "atualização de base" do automanager. A API, que antes registrava só clientes, documentos, telefones e endereços, agora atende à operação de uma loja de manutenção veicular e venda de autopeças:

- cadastro de **empresas**, as unidades comerciais;
- **usuários** associados a empresas, com perfis de **cliente**, **funcionário** e **fornecedor**;
- **veículos**, **mercadorias**, **serviços** e **vendas**;
- **credenciais** de acesso dos usuários.

Tudo tem CRUD completo e HATEOAS (HAL e HAL-FORMS). O projeto foi desenvolvido com Java, Spring Boot, Spring Data JPA, Spring HATEOAS e H2.

O modelo segue o diagrama de classes e o código sugerido pelo professor ([atviii-autobots-microservico-spring](https://github.com/gerson-pn/atviii-autobots-microservico-spring)). As mudanças em relação a esse código estão em [Decisões](#decisões).

## Como executar

É necessário ter JDK 17 ou superior e a variável `JAVA_HOME` apontando para a pasta do JDK. O Maven Wrapper está incluído; a primeira execução precisa de internet para baixar as dependências.

Dentro da pasta `automanager`, execute no PowerShell (ou dê dois cliques em `iniciar.cmd`):

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux:

```bash
./mvnw spring-boot:run
```

A porta configurada é **8081**. Para escolher outra porta:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8082"
```

Acesse http://localhost:8081/ para descobrir os links da API. Mantenha o terminal aberto; para encerrar, pressione `Ctrl+C`.

Para executar os testes e gerar o JAR:

```powershell
.\mvnw.cmd clean verify
java -jar target/automanager-0.0.1-SNAPSHOT.jar
```

O banco H2 fica na pasta `dados` (arquivo `automanager-atviii`) e mantém os registros entre execuções. Ele é separado do banco da ATVII, cujo modelo era diferente.

### Dados de exemplo

Na primeira execução, com o banco vazio, a aplicação cadastra os dados do exemplo do professor, adaptados:

- as empresas "Car service toyota ltda" e "Auto center volkswagen ltda";
- o funcionário Dom Pedro, o fornecedor "Componentes varejo de partes automotivas" e o cliente Dom Pedro;
- um Corolla Cross, uma roda de liga leve, quatro serviços e duas vendas.

O cliente fica associado às duas empresas. Para iniciar com o banco vazio, use `--automanager.dados-exemplo=false` ou apague a pasta `dados`.

## Modelo

| Classe | Conteúdo |
|---|---|
| `Empresa` | Razão social, nome fantasia, data de cadastro, endereço e telefones. Tem usuários associados, mercadorias, serviços e vendas. |
| `Usuario` | Nome, nome social, perfis (`CLIENTE`, `FUNCIONARIO`, `FORNECEDOR`), endereço, telefones, documentos, e-mails e credenciais. Um usuário pode ter mais de um perfil e estar associado a mais de uma empresa. |
| `Credencial` | Classe abstrata com criação, último acesso e situação (`inativo`). Subclasses: `CredencialUsuarioSenha` (nome de usuário e senha) e `CredencialCodigoBarra` (código do crachá). |
| `Documento` | Tipo (`CPF`, `CNPJ`, `RG`, `CNH`, `PASSAPORTE`), número único e data de emissão. |
| `Telefone`, `Email`, `Endereco` | Dados de contato. Telefones e endereço podem pertencer a um usuário ou a uma empresa. |
| `Veiculo` | Tipo (`HATCH`, `SEDA`, `SUV`, `PICKUP`, `SW`), modelo, placa única e proprietário (cliente). |
| `Mercadoria` | Nome, descrição, quantidade, valor, datas de fabricação, validade e cadastro, empresa e fornecedor opcional. |
| `Servico` | Nome, descrição, valor e empresa. |
| `Venda` | Identificação única, data, empresa, cliente, funcionário, veículo opcional, mercadorias, serviços e valor total calculado. |

### Regras de negócio

- **Cliente e funcionário da venda:** precisam estar associados à empresa da venda e ter o perfil correspondente.
- **Itens da venda:** mercadorias e serviços precisam ser da mesma empresa. A venda precisa ter pelo menos um item.
- **Veículo da venda:** é opcional; se informado, precisa pertencer ao cliente.
- **Fornecedor da mercadoria:** precisa ter o perfil `FORNECEDOR` e estar associado à empresa.
- **Proprietário do veículo:** precisa ter o perfil `CLIENTE`.
- **Valores únicos:** número de documento, placa, identificação da venda, nome de usuário e código de barras são únicos. Repetir qualquer um deles retorna 409.
- **Histórico de vendas:** é preservado. Não é possível excluir nada que apareça em uma venda: usuários, veículos, mercadorias e serviços. Também não é possível desassociar o usuário da empresa onde ele participa de vendas, nem remover dele um perfil em uso. Todos esses casos retornam 409.
- **Exclusão de empresa:** remove as vendas, mercadorias e serviços dela; os usuários são apenas desassociados.
- **Exclusão de usuário:** remove os dados pessoais, credenciais e veículos dele.
- **Senhas:** são guardadas somente como hash BCrypt e nunca aparecem nas respostas.

## Rotas

URL base: `http://localhost:8081`. Envie os corpos das requisições com `Content-Type: application/json`.

Todo recurso tem as cinco operações:

| Operação | Método e rota |
|---|---|
| Listar | GET `/{coleção}` |
| Consultar | GET `/{coleção}/{id}` |
| Cadastrar | POST `/{coleção}` |
| Atualizar | PUT `/{coleção}/{id}` |
| Excluir | DELETE `/{coleção}/{id}` |

Alguns recursos exigem um parâmetro no cadastro e aceitam filtros na listagem:

| Coleção | Parâmetro obrigatório no POST | Filtros do GET da coleção |
|---|---|---|
| `/empresas` | — | `usuarioId` (empresas do usuário) |
| `/usuarios` | — (`empresaId` opcional já associa o usuário) | `perfil`, `empresaId` |
| `/telefones` | `usuarioId` **ou** `empresaId` | `usuarioId`, `empresaId` |
| `/enderecos` | `usuarioId` **ou** `empresaId` (um endereço por dono) | `usuarioId`, `empresaId` |
| `/documentos` | `usuarioId` | `usuarioId` |
| `/emails` | `usuarioId` | `usuarioId` |
| `/credenciais` | `usuarioId` | `usuarioId` |
| `/veiculos` | `proprietarioId` | `proprietarioId` |
| `/mercadorias` | `empresaId` | `empresaId`, `fornecedorId` |
| `/servicos` | `empresaId` | `empresaId` |
| `/vendas` | `empresaId` | `empresaId`, `clienteId`, `funcionarioId`, `veiculoId` |

Associação de usuários a empresas:

| Método | Rota | Operação |
|---|---|---|
| PUT | `/empresas/{id}/usuarios/{usuarioId}` | Associar usuário existente à empresa. Repetir não duplica. |
| DELETE | `/empresas/{id}/usuarios/{usuarioId}` | Desassociar usuário da empresa. |
| GET | `/usuarios?empresaId={id}` | Usuários da empresa. Combine com `&perfil=FUNCIONARIO`, por exemplo. |

Um filtro com ID inexistente retorna 404. Um dono existente sem registros retorna uma coleção vazia com 200.

## Exemplos de JSON

IDs e datas de cadastro são gerados pelo servidor e não devem ser enviados.

### Empresa — POST /empresas

```json
{
  "razaoSocial": "Car service toyota ltda",
  "nomeFantasia": "Car service manutenção veicular",
  "endereco": {
    "estado": "São Paulo", "cidade": "São Paulo", "bairro": "Centro",
    "rua": "Av. São João", "numero": "00", "codigoPostal": "01035-000"
  },
  "telefones": [{ "ddd": "011", "numero": "986454527" }]
}
```

PUT `/empresas/{id}` altera só `razaoSocial` e `nomeFantasia`. Endereço e telefones têm rotas próprias.

### Usuário — POST /usuarios?empresaId={empresaId}

```json
{
  "nome": "Pedro Alcântara de Bragança e Bourbon",
  "nomeSocial": "Dom Pedro",
  "perfis": ["FUNCIONARIO"],
  "endereco": {
    "estado": "São Paulo", "cidade": "São Paulo", "bairro": "Jardins",
    "rua": "Av. São Gabriel", "numero": "00", "codigoPostal": "01435-001"
  },
  "telefones": [{ "ddd": "011", "numero": "9854633728" }],
  "documentos": [{ "tipo": "CPF", "numero": "856473819229", "dataEmissao": "2020-01-10" }],
  "emails": [{ "endereco": "a@a.com" }]
}
```

Só `nome` e `perfis` são obrigatórios. PUT `/usuarios/{id}` recebe `nome`, `nomeSocial` e `perfis`.

### Credencial — POST /credenciais?usuarioId={usuarioId}

```json
{ "tipo": "USUARIO_SENHA", "nomeUsuario": "dompedrofuncionario", "senha": "123456" }
```

```json
{ "tipo": "CODIGO_BARRA", "codigo": 7891234567895 }
```

A senha precisa ter de 6 a 72 caracteres. No PUT, o `tipo` não pode mudar. A senha pode ser omitida para manter a atual, e `"inativo": true` desativa a credencial. A resposta nunca traz a senha:

```json
{ "id": 1, "tipo": "USUARIO_SENHA", "nomeUsuario": "dompedrofuncionario", "criacao": "2026-11-01T13:17:26.580-03:00", "inativo": false }
```

### Veículo — POST /veiculos?proprietarioId={clienteId}

```json
{ "tipo": "SUV", "modelo": "corolla-cross", "placa": "ABC-0000" }
```

A placa aceita os formatos antigo (`ABC-1234`) e Mercosul (`ABC1D23`) e é gravada em maiúsculas.

### Mercadoria — POST /mercadorias?empresaId={empresaId}

```json
{
  "nome": "Roda de liga leve modelo toyota etios",
  "descricao": "Roda original de fábrica para modelos hatch",
  "quantidade": 30,
  "valor": 300.00,
  "fabricacao": "2026-09-01",
  "validade": "2031-09-01",
  "fornecedorId": 2
}
```

### Serviço — POST /servicos?empresaId={empresaId}

```json
{ "nome": "Alinhamento de rodas", "descricao": "Alinhamento das rodas do carro", "valor": 50.00 }
```

### Venda — POST /vendas?empresaId={empresaId}

```json
{
  "identificacao": "1234698745",
  "clienteId": 3,
  "funcionarioId": 1,
  "veiculoId": 1,
  "mercadoriaIds": [1],
  "servicoIds": [1, 2]
}
```

`identificacao` e `veiculoId` são opcionais; sem identificação, o servidor gera uma. A resposta resume a venda e calcula o total:

```json
{
  "id": 1,
  "identificacao": "1234698745",
  "cadastro": "2026-11-01T13:17:26.854-03:00",
  "cliente": { "id": 3, "nome": "Pedro Alcantara de Braganca e Bourbon" },
  "funcionario": { "id": 1, "nome": "Pedro Alcantara de Braganca e Bourbon" },
  "veiculo": { "id": 1, "placa": "ABC-0000", "modelo": "corolla-cross" },
  "mercadorias": [{ "id": 1, "nome": "Roda de liga leve modelo toyota etios", "valor": 300.00 }],
  "servicos": [{ "id": 1, "nome": "Troca de rodas", "valor": 50.00 }, { "id": 2, "nome": "Alinhamento de rodas", "valor": 50.00 }],
  "valorTotal": 400.00,
  "_links": {
    "self": { "href": "http://localhost:8081/vendas/1" },
    "empresa": { "href": "http://localhost:8081/empresas/1" },
    "cliente": { "href": "http://localhost:8081/usuarios/3" },
    "funcionario": { "href": "http://localhost:8081/usuarios/1" },
    "veiculo": { "href": "http://localhost:8081/veiculos/1" }
  }
}
```

PUT `/vendas/{id}` recebe o mesmo corpo e substitui pessoas, veículo e itens. A empresa da venda não muda.

### Documento, telefone, e-mail e endereço

```json
{ "tipo": "CPF", "numero": "12584698533", "dataEmissao": "2020-01-10" }
```

```json
{ "ddd": "011", "numero": "986454527" }
```

```json
{ "endereco": "c@c.com" }
```

O endereço usa os mesmos campos do exemplo de empresa. `informacoesAdicionais` é o único campo opcional.

## HATEOAS

Envie `Accept: application/hal+json` para receber links, ou `Accept: application/prs.hal-forms+json` para receber também os formulários (`_templates`) com método, campos e destino de cada operação.

- A raiz `/` aponta para todas as coleções.
- Cada registro tem `self`, um link para sua coleção e `inicio`.
- Uma empresa aponta para seus usuários, telefones, endereço, mercadorias, serviços e vendas.
- Um usuário aponta para suas empresas, telefones, documentos, e-mails, credenciais e endereço. Conforme o perfil, também aponta para `veiculos` e `compras` (cliente), `vendasRealizadas` (funcionário) ou `mercadoriasFornecidas` (fornecedor).
- Telefones, documentos, e-mails, endereços e credenciais apontam para o dono (`usuario` ou `empresa`).
- Mercadorias apontam para `empresa` e `fornecedor`; serviços, para `empresa`; veículos, para `proprietario` e suas `vendas`.
- Vendas apontam para empresa, cliente, funcionário, veículo e cada mercadoria e serviço.

Em HAL-FORMS, o template `default` de um registro é o PUT e `excluir...` é o DELETE. Os cadastros relacionados aparecem com nomes próprios, por exemplo `criarVeiculo`, `criarCredencial` e `criarVenda`, e o `target` já traz o parâmetro do dono. O cadastro de endereço só é oferecido enquanto o dono não tem um.

### Roteiro para conferir

1. Inicie a aplicação e faça GET `/`.
2. Siga `empresas` e abra a primeira empresa. Siga `usuarios` e `vendas`.
3. Abra uma venda e siga os links `cliente`, `veiculo` e `servico`.
4. Cadastre uma empresa nova e um cliente com POST `/usuarios?empresaId={id}`.
5. Cadastre um veículo para o cliente, um funcionário, uma mercadoria e um serviço, e registre uma venda.
6. Tente excluir a mercadoria vendida e confira o 409. Exclua a venda e repita: agora a exclusão funciona.
7. Cadastre uma credencial com senha e confira que a resposta não traz a senha.

## Respostas

- `200`: consulta ou atualização realizada.
- `201`: cadastro criado, com o registro no corpo e a URL dele no cabeçalho `Location`.
- `204`: exclusão ou desassociação realizada, sem corpo.
- `400`: dados inválidos ou regra de negócio violada, como cliente não associado à empresa ou veículo de outro cliente.
- `404`: registro não encontrado.
- `409`: conflito, como valor único repetido, segundo endereço para o mesmo dono ou exclusão de registro presente em vendas.

Os erros usam `application/problem+json`, com `status`, `title` e `detail`; erros de validação incluem `campos`.

## Decisões

Em relação ao código sugerido pelo professor:

- **Pacotes e versões:** o projeto continua em Spring Boot 3 (`jakarta.persistence`), como na ATVII. O pacote `entitades` virou `entidades`, e o campo `fabricao` virou `fabricacao`.
- **Vendas:** o código sugerido ligava cada mercadoria a uma única venda (`@OneToMany`) e cada veículo a uma única venda (`@OneToOne`), por isso precisava criar uma "roda 2" para a segunda venda. Aqui a venda referencia o catálogo da empresa (`@ManyToMany`), e um veículo pode ter várias vendas.
- **Usuários e empresas:** um usuário pode estar associado a várias empresas, por exemplo um cliente atendido tanto na rede Toyota quanto na Volkswagen.
- **Chaves estrangeiras:** mercadorias, serviços, vendas e veículos guardam a empresa ou o dono. As coleções do diagrama (`Empresa.mercadorias`, `Usuario.veiculos` e outras) continuam nas classes como lado inverso.
- **Valores e enumerações:** valores monetários usam `BigDecimal`, para não haver erro de arredondamento, e enumerações são gravadas pelo nome.
- **Requisições:** o corpo das requisições usa classes próprias (pacote `requisicoes`), separadas das entidades. Assim, os dados são validados antes de chegar ao banco e os formulários HAL-FORMS mostram os campos certos.
- **Banco:** o código sugerido usa MySQL. O projeto continua com H2 em arquivo, como na ATVII, para rodar sem instalar banco.
- **Credenciais:** a senha é guardada com hash BCrypt (`spring-security-crypto`). O login e as permissões por perfil ficam para a "atualização de segurança".

### Limites

- **Estoque:** vender não altera a quantidade da mercadoria. A venda referencia o item do catálogo sem quantidade por item.
- **Valor da venda:** o total é calculado com os valores atuais dos itens. Alterar o preço de uma mercadoria muda o total das vendas antigas que a contêm.
- **Segurança:** não há autenticação; qualquer pessoa com acesso à porta pode usar a API. Isso faz parte da próxima etapa.

## Organização

- `entidades` e `enumeracoes`: modelo persistido.
- `requisicoes`: dados aceitos nos cadastros e atualizações, com validação.
- `repositorios`: consultas.
- `servicos`: regras de negócio e transações.
- `controles`: rotas, códigos HTTP e tratamento de erros.
- `representacoes`: links e formulários HATEOAS e as respostas de venda e credencial.
- `configuracao`: HAL/HAL-FORMS, codificador de senha e dados de exemplo.

## Testes

`.\mvnw.cmd test` executa 11 testes com H2 em memória:

- `AutomanagerApplicationTests`: inicialização da aplicação.
- `AutomanagerApiTests`: nove cenários de ponta a ponta por HTTP. Cobrem:
  - o CRUD de cada recurso;
  - a associação de usuários a empresas;
  - os perfis;
  - os links e formulários;
  - as regras de venda;
  - os conflitos;
  - a exclusão em cascata;
  - o formato dos erros;
  - a verificação de que a senha não aparece na resposta e fica guardada como hash.
- `DadosExemploTests`: os dados de exemplo são cadastrados uma única vez e as vendas totalizam R$ 400,00 e R$ 380,00.
