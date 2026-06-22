# Estofados e Cosidos

Sistema de gestão de produção para uma empresa fictícia da indústria automóvel dedicada à produção de estofos automóveis.

O projeto foi desenvolvido no âmbito da Unidade Curricular de Projeto II.

## Objetivo

O objetivo do sistema é suportar digitalmente os principais processos operacionais da fábrica, desde a receção de matéria-prima até à produção, controlo de qualidade, logística interna e expedição de produto acabado.

O sistema pretende acompanhar o fluxo de produção de forma estruturada, garantindo rastreabilidade entre ordens de fabrico, semiacabados, produto acabado, stock, carrinhos de abastecimento e registos de qualidade.

## Tecnologias

* Java 21
* Spring Boot
* Spring Web MVC
* Thymeleaf
* Spring Data JPA
* Hibernate
* PostgreSQL
* Flyway
* Maven
* Docker Compose
* Lombok

## Requisitos

Antes de correr o projeto, é necessário ter instalado:

* Java 21
* Maven Wrapper incluído no projeto
* Docker Desktop
* Git

## Configuração inicial

Clonar o repositório:

```bash
git clone https://github.com/tiagoavrodrigues/estofados-e-cosidos.git
cd estofados-e-cosidos
```

Criar o ficheiro `.env` a partir do exemplo:

```bash
cp .env.example .env
```

No Windows PowerShell:

```powershell
copy .env.example .env
```

O ficheiro `.env` contém as variáveis locais usadas pelo Docker Compose e pela aplicação.

Exemplo:

```env
DB_NAME=estofadosecosidos
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_PORT=5432
DB_URL=jdbc:postgresql://localhost:5433/estofadosecosidos
```

O ficheiro `.env` não deve ser versionado no Git.

## Base de dados

A base de dados PostgreSQL corre em Docker.

Para iniciar os serviços:

```bash
docker compose up -d
```

Para parar os serviços:

```bash
docker compose down
```

Para verificar se o container está ativo:

```bash
docker ps
```

Por defeito, o PostgreSQL fica disponível em:

```text
localhost:5433
```

Dentro do container, o PostgreSQL continua a usar a porta interna `5432`.

## Executar a aplicação

Para correr a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

A aplicação ficará disponível em:

```text
http://localhost:8080
```

## Compilar o projeto

Para compilar sem executar testes:

```bash
./mvnw clean package -DskipTests
```

No Windows PowerShell:

```powershell
.\mvnw clean package -DskipTests
```

## Estrutura inicial do projeto

```text
src/
├── main/
│   ├── java/
│   │   └── com/example/estofadosecosidos/
│   │       └── EstofadosecosidosApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/example/estofadosecosidos/
            └── EstofadosecosidosApplicationTests.java
```

## Configuração da aplicação

A aplicação usa variáveis de ambiente com valores por defeito.

Exemplo em `application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5433/estofadosecosidos}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
```

Isto permite correr o projeto localmente sem expor credenciais reais no repositório.

## Convenção de commits

Este projeto usa Conventional Commits.

Exemplos:

```text
chore: initial project setup
docs: add project setup instructions
feat: add manufacturing order entity
fix: correct stock validation
refactor: reorganize service layer
test: add order service tests
```

Tipos principais:

* `feat`: nova funcionalidade
* `fix`: correção de erro
* `docs`: documentação
* `style`: formatação sem alteração de lógica
* `refactor`: alteração interna sem mudar comportamento
* `test`: testes
* `chore`: configuração, manutenção ou tarefas auxiliares
* `build`: alterações em Maven, Docker ou dependências
* `ci`: configuração de integração contínua

## Estado atual

O projeto encontra-se na fase inicial de configuração técnica.

Funcionalidades futuras previstas:

* Gestão de encomendas
* Gestão de stock
* Gestão de ordens de fabrico
* Corte de matéria-prima
* Logística interna e carrinhos
* Associação entre semiacabado e produto acabado
* Registos de qualidade
* Embalagem e expedição
* Interface Web com Thymeleaf
* Possível interface Desktop com JavaFX ou Swing

## Autores

* Tiago Rodrigues
