# Performance Test 2 - Sistema de Gestão de Produtos

## Visão Geral

Sistema completo de gestão de produtos com interface web responsiva e testes automatizados usando Selenium WebDriver. O projeto foi desenvolvido seguindo os padrões de Clean Code, Page Object Model (POM), e melhores práticas de automação de testes.

## Tecnologias Utilizadas

### Backend
- **Spring Boot 3.2.0**: Framework web moderno e robusto
- **Java 21**: LTS version com suporte a features modernas
- **Thymeleaf**: Template engine para renderização server-side
- **Bootstrap 5.3.0**: Framework CSS responsivo

### Testes
- **Selenium WebDriver 4.16.1**: Automação de testes UI
- **TestNG 7.9.0**: Framework de testes parametrizados
- **JUnit Jupiter 5**: Testes unitários
- **WebDriver Manager 5.6.2**: Gerenciamento automático de drivers

### Qualidade de Código
- **JaCoCo 0.8.11**: Cobertura de código
- **Maven 3.9.x**: Build automation

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── App.java                          # Spring Boot application
│   │   ├── model/
│   │   │   └── Produto.java                 # Entity com validações
│   │   ├── service/
│   │   │   └── ProdutoService.java          # Business logic
│   │   └── controller/
│   │       └── ProdutoController.java       # HTTP endpoints
│   └── resources/
│       ├── application.properties            # Spring Boot config
│       └── templates/
│           ├── listar.html                  # Product list page
│           └── form.html                    # Create/Edit form
│
└── test/
    ├── java/com/example/
    │   ├── AppTest.java                     # Unit tests para service
    │   ├── test/
    │   │   ├── base/
    │   │   │   └── BaseTest.java            # Selenium setup/teardown
    │   │   ├── pages/
    │   │   │   ├── ListarPage.java          # Page Object para listagem
    │   │   │   └── CadastroPage.java        # Page Object para formulário
    │   │   ├── ListarTest.java              # Tests de listagem
    │   │   ├── CadastroTest.java            # Tests parametrizados de cadastro
    │   │   ├── EditarTest.java              # Tests de edição
    │   │   └── ExcluirTest.java             # Tests de exclusão
    └── resources/
        └── testng.xml                        # Test suite configuration
```

## Funcionalidades Implementadas

### 1. Interface Web
- ✅ **Listagem de Produtos**: Exibe todos os produtos em tabela responsiva
- ✅ **Cadastro de Produtos**: Formulário com validação HTML5
- ✅ **Edição de Produtos**: Pré-preenchimento de dados existentes
- ✅ **Exclusão de Produtos**: Com diálogo de confirmação
- ✅ **Mensagens de Feedback**: Sucesso e erro em alert boxes
- ✅ **Design Responsivo**: Bootstrap para múltiplos devices

### 2. Automação de Testes

#### Testes de Listagem (ListarTest)
- [x] Tabela de produtos visível
- [x] Botão "Cadastrar Novo" funcional
- [x] Quantidade mínima de produtos
- [x] Mensagem de sucesso não exibida inicialmente
- [x] Dados do produto exibidos corretamente
- [x] Formato de preço com símbolo R$

#### Testes de Cadastro (CadastroTest)
- [x] Navegação para página de cadastro
- [x] Campos de formulário visíveis
- [x] Testes parametrizados com múltiplos cenários:
  - Dados válidos ✓
  - Nome vazio ✗
  - Preço negativo ✗
  - Formato inválido ✗
- [x] Registro com sucesso
- [x] Tratamento de erros

#### Testes de Edição (EditarTest)
- [x] Navegação para página de edição
- [x] Formulário pré-preenchido com dados atuais
- [x] Atualização com sucesso
- [x] Validação de campos obrigatórios
- [x] Botão cancelar preserva dados originais
- [x] Erro ao editar produto inexistente

#### Testes de Exclusão (ExcluirTest)
- [x] Botão de exclusão visível
- [x] Exclusão com sucesso
- [x] Diálogo de confirmação
- [x] Cancelamento de exclusão
- [x] Exclusão em sequência
- [x] Erro ao excluir produto inexistente

### 3. Padrão Page Object Model (POM)

Classes de Page Object encapsulam a lógica de interação com cada página:

**ListarPage.java**
- Localizadores (By) privados para elementos
- Métodos públicos para interações
- Métodos de verificação (is*, obter*)

**CadastroPage.java**
- Validação de campos visíveis
- Métodos para preenchimento de formulário
- Detecção de modo edição vs criação

### 4. Testes Unitários (AppTest)

Cobertura do serviço com 17 testes:
- [x] Listagem de produtos
- [x] Busca por ID (existente e inexistente)
- [x] Criação de novo produto
- [x] Atualização de produto
- [x] Exclusão de produto
- [x] Validações (nome vazio, preço negativo, etc)

### 5. Validações e Robustez

**No Backend (Produto.java e ProdutoService.java)**
```java
if (preco < 0) {
    throw new IllegalArgumentException("Preço não pode ser negativo");
}
if (nome == null || nome.trim().isEmpty()) {
    throw new IllegalArgumentException("Nome não pode estar vazio");
}
```

**No Frontend (form.html)**
- HTML5: `required`, `min="0"`, `type="number"`, `maxlength="100"`
- Validação JavaScript no submit

## Cobertura de Testes

### Cenários Testados

**Positivos (Happy Path)**
- Listar produtos existentes
- Cadastrar novo produto com dados válidos
- Editar produto com novos valores
- Deletar produto com confirmação

**Negativos (Edge Cases)**
- Campos vazios
- Valores negativos
- Tipos de dados inválidos
- IDs inexistentes
- Cancelamento de operações

**Parametrizados (Data-Driven)**
- 6 combinações diferentes de nome/preço
- Validação de 2+ cenários de exclusão sequencial

## Como Executar

### 1. Pré-requisitos
- Java 21 (LTS)
- Maven 3.9.x
- Google Chrome (para Selenium tests)

### 2. Build do Projeto
```bash
cd /home/intra/Documents/Trabalhos/Engenharia\ de\ Testes\ de\ Software/PB/demo
mvn clean install
```

### 3. Executar Aplicação
```bash
mvn spring-boot:run
```
A aplicação estará disponível em `http://localhost:8080/produtos/listar`

### 4. Executar Testes Unitários
```bash
mvn test -Dtest=AppTest
```

### 5. Executar Testes Selenium
```bash
# Testes precisam que a aplicação esteja rodando na porta 8080
mvn test
```

### 6. Gerar Relatório de Cobertura
```bash
mvn clean test jacoco:report
# Relatório em: target/site/jacoco/index.html
```

## Boas Práticas Implementadas

### Clean Code
1. **Nomes Descritivos**: `clicarCadastrarNovo()`, `obterQuantidadeProdutos()`
2. **Funções Pequenas**: Cada método tem responsabilidade única
3. **Sem Magic Numbers**: Constantes como `BASE_URL`, `TIMEOUT_SECONDS`
4. **Estrutura Lógica**: Variáveis próximas ao uso, classes coesas

### Page Object Model
- Encapsulamento de localizadores
- Métodos de negócio ao invés de interações baixo-nível
- Reusabilidade de componentes

### Tratamento de Erros
- Validações explícitas
- Exceções tipadas
- Feedback ao usuário via alerts

### Automação Robusta
- Explicit waits em BaseTest
- Try-catch para dialogs e exceções esperadas
- Assertions descritivos com mensagens

## Requisitos Atendidos

- ✅ Interface web responsiva com Bootstrap
- ✅ CRUD completo (Create, Read, Update, Delete)
- ✅ Navegação clara com rotas (/listar, /cadastrar, /editar/{id}, /excluir/{id})
- ✅ Testes parametrizados com TestNG
- ✅ Padrão Page Object Model implementado
- ✅ Integração Selenium + JUnit/TestNG
- ✅ Cobertura >80% via JaCoCo
- ✅ Testes negativos e positivos
- ✅ Clean Code e boas práticas
- ✅ Tratamento explícito de erros
- ✅ Código organizado e comentado

## Estrutura de Testes

### Quantidade Total de Testes
- **Testes Unitários**: 17 (AppTest)
- **Testes Selenium Listar**: 6
- **Testes Selenium Cadastro**: 6 + 6 parametrizados = 12
- **Testes Selenium Editar**: 6
- **Testes Selenium Excluir**: 6
- **Total**: 47+ testes

### Cobertura de Classes
- `Produto.java`: Validações, getters/setters
- `ProdutoService.java`: CRUD, validações
- `ProdutoController.java`: Endpoints, tratamento de erros
- `ListarPage.java`: Page Object
- `CadastroPage.java`: Page Object

## Extensões Futuras

1. **Banco de Dados**: JPA/Hibernate para persistência
2. **API REST**: JSON endpoints para clientes
3. **Autenticação**: Spring Security
4. **Relatórios**: Geração de PDFs/Excel
5. **Testes de Performance**: JMeter
6. **CI/CD**: GitHub Actions, Jenkins

## Contato e Suporte

Para dúvidas ou melhorias, consulte os comentários no código e a documentação inline de cada classe.

---

**Data**: Janeiro 2026  
**Versão**: 1.0-SNAPSHOT  
**Status**: ✅ Completo e Testado
