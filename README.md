# 🧾 Sistema de Gestão de Pedidos (Console)

Projeto final desenvolvido em **Java**, com foco em **POO**, **concorrência**, **tratamento de exceções** e **boas práticas de arquitetura (SOLID e Object Calisthenics)**.

---

## 🚀 Como compilar e executar (linha de comando)

Este repositório armazenou os fontes em `app/` na raiz — as instruções abaixo estão adaptadas ao layout atual. Se você usar uma estrutura de pacotes diferente (ex.: `src/`), ajuste os comandos conforme necessário.

1. (Opcional) Vá para a pasta do projeto:

```powershell
cd C:\Users\Usuario\Documents\pedido-app
```

2. Compile o projeto (padrão atual com fontes em `app/`):

```powershell
javac -d out app\*.java
```

3. Execute:

```powershell
java -cp out app.Main
```

💡 Dica: Se preferir, organize os arquivos em pacotes (`app.service`, `app.model`, etc.) e abra o projeto em uma IDE como **IntelliJ IDEA** ou **Eclipse** — isso facilita navegação, depuração e execução.

---

## 🧠 O que o sistema faz

* **Cadastro de Clientes**

  * Atributos: `ID`, `nome`, `email`
  * Inclui validações de dados (classe `Cliente` e `ValidacaoException`).

* **Cadastro de Produtos**

  * Atributos: `ID`, `nome`, `preço`, `categoria` (`Categoria` enum)
  * Inclui validações de dados (classe `Produto`).

* **Criação de Pedidos**

  * Cada `Pedido` contém **itens (ItemPedido = produto + quantidade)**.
  * Cálculo de total via `Pedido.calcularTotal()`.

* **Processamento Assíncrono de Pedidos**

  * Pedidos entram em uma **fila** gerenciada pelo `PedidoProcessor` (usa `LinkedBlockingQueue`).
  * Uma **thread** consome a fila e atualiza o status do pedido: `PROCESSANDO` → (simula trabalho) → `FINALIZADO`.

* **Listagens disponíveis:**

  * Clientes
  * Produtos
  * Pedidos (com status atual)

---

## 🧩 Conceitos de POO aplicados

* **Classes e objetos:** `Cliente`, `Produto`, `Pedido`, `ItemPedido`.

* **Encapsulamento:** atributos `private`, métodos públicos bem definidos; coleções expostas via `Collections.unmodifiableList`.

* **Herança e Polimorfismo:** projeto preparado para extensão (ex.: `DigitalProduct extends Produto`).

* **Interfaces e classes abstratas:** `Identificavel` como contrato para entidades com `getId()`.

---

## 🧱 Princípios SOLID

* **S — Single Responsibility:** cada classe concentra responsabilidade única (modelo, validação, processamento, menu).

* **O — Open/Closed:** arquitetura permite extensão (novo status, nova categoria, tipos de produto) sem alterar código existente.

* **L — Liskov Substitution:** possibilidade de substituir `Produto` por subtipos mantendo comportamento.

* **I — Interface Segregation:** interfaces simples e específicas (`Identificavel`).

* **D — Dependency Inversion:** possibilidade de injetar dependências (ex.: `PedidoProcessor`, repositórios) em serviços/menu.

---

## ⚙️ Object Calisthenics (mínimo de 3 regras aplicadas)

1. **Classes pequenas:** classes como `Cliente`, `Produto`, `ItemPedido` têm responsabilidades limitadas.

2. **Métodos curtos:** funções em `Main` e utilitários realizam tarefas simples (ler/validar/mostrar).

3. **Sem getters/setters triviais:** apenas o necessário é exposto; listas retornadas são imutáveis.

---

## 🚨 Tratamento de Exceções

* `ValidacaoException` → erros de validação (entrada inválida, regras de negócio).
* `InterruptedException` → interrupção/control shutdown da thread de processamento (quando usada).

> Observação: no código atual, `ValidacaoException` é o tipo usado para violações de regra — ao refatorar para um design mais completo, pode-se introduzir `InvalidDataException` e `EntityNotFoundException` conforme o padrão acima.

---

## ⚔️ Concorrência

* **Fila de pedidos:** `LinkedBlockingQueue<Pedido>` (thread-safe) usada em `PedidoProcessor`.
* **Processamento:** implementação atual usa uma `Thread` que executa `PedidoProcessor.run()`; pode ser substituída por `ExecutorService` para maior controle.
* **Controle de estado:** `Pedido.setStatus(...)` atualiza o status; ao refatorar para multi-threading intensivo, avalie uso de sincronização ou `AtomicReference` para segurança.

---

## 📚 Estrutura de arquivos (atual)

```
app/
├── Main.java
├── Categoria.java
├── Cliente.java
├── Identificavel.java
├── ItemPedido.java
├── Pedido.java
├── PedidoProcessor.java
├── Produto.java
├── StatusPedido.java
└── ValidacaoException.java
dados.json
```

Se quiser reorganizar em pacotes mais granulares, uma estrutura sugerida (opcional) é a seguinte:

```
app/
├── Main.java
├── service/
│   └── MenuService.java
├── model/
│   ├── Cliente.java
│   ├── Produto.java
│   ├── Pedido.java
│   └── ItemPedido.java
├── model/enums/
│   └── StatusPedido.java
├── repository/
│   └── (repositórios caso sejam adicionados)
└── exception/
    └── ValidacaoException.java
```

---

## 👥 Autores

* **Felipe Ismael**
* **Luiz Henrique Brites**
* **Priscila Camargo|**

📍 Projeto desenvolvido para fins de aprendizado e prática dos conceitos de **Programação Orientada a Objetos** em Java.

---
