package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Ponto de entrada da aplicação de console responsável por orquestrar cadastros,
 * criação de pedidos, processamento assíncrono e persistência em JSON.
 */
public class Main {
    private static List<Cliente> clientes = new ArrayList<>();
    private static List<Produto> produtos = new ArrayList<>();
    private static List<Pedido> pedidos = new ArrayList<>();
    private static PedidoProcessor processor = new PedidoProcessor();

    /**
     * Inicializa a thread de processamento de pedidos, carrega dados persistidos e
     * apresenta o menu interativo até que o usuário solicite a saída.
     */
    public static void main(String[] args) {
        new Thread(processor).start();
        carregarDados();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1. Cadastrar Cliente");
            System.out.println("2. Cadastrar Produto");
            System.out.println("3. Criar Pedido");
            System.out.println("4. Listar Clientes");
            System.out.println("5. Listar Produtos");
            System.out.println("6. Listar Pedidos");
            System.out.println("7. Salvar e Sair");
            System.out.print("Escolha: ");

            int opcao = lerInteiro(sc);

            switch (opcao) {
                case 1 -> cadastrarCliente(sc);
                case 2 -> cadastrarProduto(sc);
                case 3 -> criarPedido(sc);
                case 4 -> listarClientes();
                case 5 -> listarProdutos();
                case 6 -> listarPedidos();
                case 7 -> {
                    salvarDados();
                    System.out.println("Até logo!");
                    System.exit(0);
                }
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    /**
     * Solicita os dados obrigatórios do cliente e registra um novo cliente caso as
     * validações sejam atendidas.
     */
    private static void cadastrarCliente(Scanner sc) {
        System.out.print("Nome: ");
        String nome = sc.nextLine();
        System.out.print("E-mail: ");
        String email = sc.nextLine();
        try {
            Cliente c = new Cliente(gerarNovoId(clientes), nome, email);
        clientes.add(c);
        System.out.println("Cliente cadastrado!");
        } catch (ValidacaoException e) {
            System.out.println("Falha ao cadastrar cliente: " + e.getMessage());
        }
    }

    /**
     * Realiza o cadastro de um produto com validação de preço e categoria.
     */
    private static void cadastrarProduto(Scanner sc) {
        System.out.print("Nome: ");
        String nome = sc.nextLine();
        System.out.print("Preço: ");
        double preco = lerDouble(sc);
        System.out.print("Categoria (ALIMENTOS, ELETRONICOS, LIVROS): ");
        try {
            Categoria cat = Categoria.valueOf(sc.nextLine().trim().toUpperCase());
            Produto p = new Produto(gerarNovoId(produtos), nome, preco, cat);
        produtos.add(p);
        System.out.println("Produto cadastrado!");
        } catch (IllegalArgumentException e) {
            System.out.println("Categoria inválida.");
        } catch (ValidacaoException e) {
            System.out.println("Falha ao cadastrar produto: " + e.getMessage());
        }
    }

    /**
     * Cria um pedido vinculado a um cliente existente, permitindo adicionar itens
     * até que o usuário finalize a seleção.
     */
    private static void criarPedido(Scanner sc) {
        if (clientes.isEmpty() || produtos.isEmpty()) {
            System.out.println("Cadastre clientes e produtos antes!");
            return;
        }

        listarClientes();
        System.out.print("ID do Cliente: ");
        int id = lerInteiro(sc);
        Cliente cliente = clientes.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        if (cliente == null) {
            System.out.println("Cliente não encontrado!");
            return;
        }

        Pedido pedido = new Pedido(gerarNovoId(pedidos), cliente);

        while (true) {
            listarProdutos();
            System.out.print("ID do Produto (0 para finalizar): ");
            int pid = lerInteiro(sc);
            if (pid == 0) break;
            Produto prod = produtos.stream().filter(p -> p.getId() == pid).findFirst().orElse(null);
            if (prod == null) {
                System.out.println("Produto não encontrado!");
                continue;
            }
            System.out.print("Quantidade: ");
            int quantidade = lerInteiro(sc);
            if (quantidade <= 0) {
                System.out.println("Quantidade inválida.");
                continue;
            }
            try {
                pedido.adicionarItem(prod, quantidade);
            } catch (ValidacaoException e) {
                System.out.println("Falha ao adicionar item: " + e.getMessage());
            }
        }

        if (pedido.getItens().isEmpty()) {
            System.out.println("Pedido precisa ter pelo menos um item.");
            return;
        }

        pedidos.add(pedido);
        processor.adicionarPedido(pedido);
        System.out.println("Pedido criado e adicionado à fila!");
    }

    /**
     * Exibe todos os clientes cadastrados no console.
     */
    private static void listarClientes() {
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        clientes.forEach(cliente -> System.out.println("Cliente [ID=" + cliente.getId() + ", Nome=" + cliente.getNome() + ", Email=" + cliente.getEmail() + "]"));
    }

    /**
     * Exibe todos os produtos cadastrados no console.
     */
    private static void listarProdutos() {
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        produtos.forEach(produto -> System.out.println("Produto [ID=" + produto.getId() + ", Nome=" + produto.getNome() + ", Preço=" + produto.getPreco() + ", Categoria=" + produto.getCategoria() + "]"));
    }

    /**
     * Exibe os pedidos cadastrados, incluindo seus itens e status atual.
     */
    private static void listarPedidos() {
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido cadastrado.");
            return;
        }
        pedidos.forEach(pedido -> {
            System.out.println(pedido);
            pedido.getItens().forEach(item -> System.out.println("  - " + item.getQuantidade() + "x " + item.getProduto().getNome() + " (" + item.calcularSubtotal() + ")"));
        });
    }

    /**
     * Lê um inteiro do console garantindo que entradas inválidas sejam
     * reprocessadas.
     */
    private static int lerInteiro(Scanner sc) {
        while (true) {
            try {
                int valor = Integer.parseInt(sc.nextLine().trim());
                return valor;
            } catch (NumberFormatException e) {
                System.out.print("Valor inválido, tente novamente: ");
            }
        }
    }

    /**
     * Lê um número decimal do console, aceitando vírgula ou ponto como separador.
     */
    private static double lerDouble(Scanner sc) {
        while (true) {
            try {
                double valor = Double.parseDouble(sc.nextLine().replace(',', '.').trim());
                return valor;
            } catch (NumberFormatException e) {
                System.out.print("Valor inválido, tente novamente: ");
            }
        }
    }

    /**
     * Gera um novo identificador incremental a partir de uma coleção de entidades
     * que expõem o método {@code getId()}.
     */
    private static <T extends Identificavel> int gerarNovoId(List<T> itens) {
        return itens.stream().mapToInt(Identificavel::getId).max().orElse(0) + 1;
    }

    private static final String ARQUIVO_DADOS = "dados.json";

    /**
     * Persiste o estado atual da aplicação em {@code dados.json}.
     */
    private static void salvarDados() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_DADOS))) {
            DadosPersistidos dados = new DadosPersistidos();
            dados.clientes = clientes;
            dados.produtos = produtos;
            dados.pedidos = pedidos;
            writer.write(JsonUtil.toJson(dados));
            System.out.println("Dados salvos em dados.json");
        } catch (IOException e) {
            System.out.println("Falha ao salvar dados: " + e.getMessage());
        }
    }

    /**
     * Carrega o conteúdo de {@code dados.json}, recriando as coleções em memória
     * e repondo pedidos pendentes na fila de processamento.
     */
    private static void carregarDados() {
        File arquivo = new File(ARQUIVO_DADOS);
        if (!arquivo.exists()) {
            System.out.println("Nenhum dado anterior encontrado.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            StringBuilder conteudo = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                conteudo.append(linha).append('\n');
            }

            DadosPersistidos dados = JsonUtil.fromJson(conteudo.toString());
            if (dados == null) {
                System.out.println("Nenhum dado anterior encontrado.");
                return;
            }

            clientes.clear();
            produtos.clear();
            pedidos.clear();

            if (dados.clientes != null) {
                clientes.addAll(dados.clientes);
            }

            if (dados.produtos != null) {
                produtos.addAll(dados.produtos);
            }

            Map<Integer, Produto> produtosPorId = new HashMap<>();
            produtos.forEach(produto -> produtosPorId.put(produto.getId(), produto));

            if (dados.pedidos != null) {
                for (Pedido pedido : dados.pedidos) {
                    pedidos.add(pedido);
                    if (pedido.getItens() != null) {
                        pedido.getItens().forEach(item -> {
                            Produto produto = produtosPorId.get(item.getProduto().getId());
                            if (produto != null) item.atualizarProduto(produto);
                        });
                    }
                }
            }

            pedidos.stream()
                   .filter(p -> p.getStatus() == StatusPedido.FILA || p.getStatus() == StatusPedido.PROCESSANDO)
                   .forEach(processor::adicionarPedido);

            System.out.println("Dados carregados!");
        } catch (RuntimeException e) {
            System.out.println("Conteúdo de dados.json inválido. Um novo arquivo será gerado ao salvar.");
        } catch (IOException e) {
            System.out.println("Falha ao carregar dados: " + e.getMessage());
        }
    }

    /**
     * Estrutura auxiliar para serializar e desserializar o conjunto completo de
     * entidades persistidas.
     */
    private static class DadosPersistidos {
        List<Cliente> clientes;
        List<Produto> produtos;
        List<Pedido> pedidos;
    }

    /**
     * Utilitário responsável por converter dados da aplicação para JSON e realizar
     * o processo inverso sem dependências externas.
     */
    private static class JsonUtil {
        /**
         * Converte a estrutura de dados em uma string JSON formatada (com
         * indentação). Essa serialização é manual: cada campo é escrito
         * explicitamente, garantindo controle sobre nomes e estrutura, além de
         * independência de bibliotecas externas.
         */
        private static String toJson(DadosPersistidos dados) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            appendClientes(sb, dados.clientes, 1);
            sb.append(",\n");
            appendProdutos(sb, dados.produtos, 1);
            sb.append(",\n");
            appendPedidos(sb, dados.pedidos, 1);
            sb.append('\n').append('}');
            return sb.toString();
        }

        /**
         * Realiza o parse de uma string JSON para objetos de domínio. Primeiro
         * converte o texto em uma combinação de Map/List/String/Number/Boolean via
         * {@link Parser}, depois mapeia para as entidades ricas (Cliente, Produto,
         * Pedido, ItemPedido), reconstituindo relacionamentos e garantindo
         * validações.
         */
        private static DadosPersistidos fromJson(String json) {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            Object parsed = new Parser(json).parseValue();
            if (!(parsed instanceof Map<?, ?> mapa)) {
                throw new IllegalArgumentException("JSON deve representar um objeto");
            }

            DadosPersistidos dados = new DadosPersistidos();
            dados.clientes = parseClientes(obterLista(mapa, "clientes"));
            dados.produtos = parseProdutos(obterLista(mapa, "produtos"));
            dados.pedidos = parsePedidos(obterLista(mapa, "pedidos"), dados.clientes, dados.produtos);
            return dados;
        }

        /** Escreve o array de clientes no JSON. */
        private static void appendClientes(StringBuilder sb, List<Cliente> clientes, int nivel) {
            indent(sb, nivel).append("\"clientes\": [");
            if (clientes != null && !clientes.isEmpty()) {
                sb.append('\n');
                for (int i = 0; i < clientes.size(); i++) {
                    Cliente c = clientes.get(i);
                    indent(sb, nivel + 1).append('{').append('\n');
                    indent(sb, nivel + 2).append("\"id\": ").append(c.getId()).append(',').append('\n');
                    indent(sb, nivel + 2).append("\"nome\": \"").append(escapar(c.getNome())).append("\",").append('\n');
                    indent(sb, nivel + 2).append("\"email\": \"").append(escapar(c.getEmail())).append("\"").append('\n');
                    indent(sb, nivel + 1).append('}');
                    if (i < clientes.size() - 1) sb.append(',');
                    sb.append('\n');
                }
                indent(sb, nivel).append(']');
            } else {
                sb.append(']');
            }
        }

        /** Escreve o array de produtos no JSON. */
        private static void appendProdutos(StringBuilder sb, List<Produto> produtos, int nivel) {
            indent(sb, nivel).append("\"produtos\": [");
            if (produtos != null && !produtos.isEmpty()) {
                sb.append('\n');
                for (int i = 0; i < produtos.size(); i++) {
                    Produto p = produtos.get(i);
                    indent(sb, nivel + 1).append('{').append('\n');
                    indent(sb, nivel + 2).append("\"id\": ").append(p.getId()).append(',').append('\n');
                    indent(sb, nivel + 2).append("\"nome\": \"").append(escapar(p.getNome())).append("\",").append('\n');
                    indent(sb, nivel + 2).append("\"preco\": ").append(p.getPreco()).append(',').append('\n');
                    indent(sb, nivel + 2).append("\"categoria\": \"").append(p.getCategoria()).append("\"").append('\n');
                    indent(sb, nivel + 1).append('}');
                    if (i < produtos.size() - 1) sb.append(',');
                    sb.append('\n');
                }
                indent(sb, nivel).append(']');
            } else {
                sb.append(']');
            }
        }

        /** Escreve o array de pedidos, aninhando cliente e itens. */
        private static void appendPedidos(StringBuilder sb, List<Pedido> pedidos, int nivel) {
            indent(sb, nivel).append("\"pedidos\": [");
            if (pedidos != null && !pedidos.isEmpty()) {
                sb.append('\n');
                for (int i = 0; i < pedidos.size(); i++) {
                    Pedido pedido = pedidos.get(i);
                    indent(sb, nivel + 1).append('{').append('\n');
                    indent(sb, nivel + 2).append("\"id\": ").append(pedido.getId()).append(',').append('\n');
                    indent(sb, nivel + 2).append("\"cliente\": {").append('\n');
                    indent(sb, nivel + 3).append("\"id\": ").append(pedido.getCliente().getId()).append(',').append('\n');
                    indent(sb, nivel + 3).append("\"nome\": \"").append(escapar(pedido.getCliente().getNome())).append("\",").append('\n');
                    indent(sb, nivel + 3).append("\"email\": \"").append(escapar(pedido.getCliente().getEmail())).append("\"").append('\n');
                    indent(sb, nivel + 2).append("},\n");
                    indent(sb, nivel + 2).append("\"itens\": [");
                    if (!pedido.getItens().isEmpty()) {
                        sb.append('\n');
                        for (int j = 0; j < pedido.getItens().size(); j++) {
                            ItemPedido item = pedido.getItens().get(j);
                            indent(sb, nivel + 3).append('{').append('\n');
                            indent(sb, nivel + 4).append("\"produto\": {").append('\n');
                            indent(sb, nivel + 5).append("\"id\": ").append(item.getProduto().getId()).append(',').append('\n');
                            indent(sb, nivel + 5).append("\"nome\": \"").append(escapar(item.getProduto().getNome())).append("\",").append('\n');
                            indent(sb, nivel + 5).append("\"preco\": ").append(item.getProduto().getPreco()).append(',').append('\n');
                            indent(sb, nivel + 5).append("\"categoria\": \"").append(item.getProduto().getCategoria()).append("\"").append('\n');
                            indent(sb, nivel + 4).append("},\n");
                            indent(sb, nivel + 4).append("\"quantidade\": ").append(item.getQuantidade()).append('\n');
                            indent(sb, nivel + 3).append('}');
                            if (j < pedido.getItens().size() - 1) sb.append(',');
                            sb.append('\n');
                        }
                        indent(sb, nivel + 2).append(']');
                    } else {
                        sb.append(']');
                    }
                    sb.append(',').append('\n');
                    indent(sb, nivel + 2).append("\"status\": \"").append(pedido.getStatus()).append("\"").append('\n');
                    indent(sb, nivel + 1).append('}');
                    if (i < pedidos.size() - 1) sb.append(',');
                    sb.append('\n');
                }
                indent(sb, nivel).append(']');
            } else {
                sb.append(']');
            }
        }

        /** Adiciona espaços para simular indentação no JSON de saída. */
        private static StringBuilder indent(StringBuilder sb, int nivel) {
            for (int i = 0; i < nivel; i++) {
                sb.append("  ");
            }
            return sb;
        }

        /** Escapa caracteres especiais de string para JSON válido. */
        private static String escapar(String valor) {
            if (valor == null) return "";
            StringBuilder sb = new StringBuilder();
            for (char c : valor.toCharArray()) {
                switch (c) {
                    case '\\' -> sb.append("\\\\");
                    case '"' -> sb.append("\\\"");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> sb.append(c);
                }
            }
            return sb.toString();
        }

        /** Obtém uma lista do mapa raiz, garantindo tipo esperado. */
        private static List<?> obterLista(Map<?, ?> mapa, String chave) {
            Object valor = mapa.get(chave);
            if (valor == null) {
                return new ArrayList<>();
            }
            if (valor instanceof List<?> lista) {
                return lista;
            }
            throw new IllegalArgumentException("Campo " + chave + " deve ser uma lista");
        }

        /** Converte a lista JSON bruta em objetos Cliente válidos. */
        private static List<Cliente> parseClientes(List<?> lista) {
            List<Cliente> clientes = new ArrayList<>();
            for (Object obj : lista) {
                if (!(obj instanceof Map<?, ?> mapa)) continue;
                int id = ((Number) mapa.get("id")).intValue();
                String nome = (String) mapa.get("nome");
                String email = (String) mapa.get("email");
                clientes.add(new Cliente(id, nome, email));
            }
            return clientes;
        }

        /** Converte a lista JSON bruta em objetos Produto válidos. */
        private static List<Produto> parseProdutos(List<?> lista) {
            List<Produto> produtos = new ArrayList<>();
            for (Object obj : lista) {
                if (!(obj instanceof Map<?, ?> mapa)) continue;
                int id = ((Number) mapa.get("id")).intValue();
                String nome = (String) mapa.get("nome");
                double preco = ((Number) mapa.get("preco")).doubleValue();
                Categoria categoria = Categoria.valueOf(((String) mapa.get("categoria")).toUpperCase());
                produtos.add(new Produto(id, nome, preco, categoria));
            }
            return produtos;
        }

        /**
         * Converte a lista JSON bruta em objetos Pedido com seus itens, ligando-os
         * às instâncias já carregadas de Cliente e Produto.
         */
        private static List<Pedido> parsePedidos(List<?> lista, List<Cliente> clientes, List<Produto> produtos) {
            Map<Integer, Cliente> clientesPorId = new HashMap<>();
            for (Cliente cliente : clientes) {
                clientesPorId.put(cliente.getId(), cliente);
            }

            Map<Integer, Produto> produtosPorId = new HashMap<>();
            for (Produto produto : produtos) {
                produtosPorId.put(produto.getId(), produto);
            }

            List<Pedido> pedidos = new ArrayList<>();
            for (Object obj : lista) {
                if (!(obj instanceof Map<?, ?> mapa)) continue;
                int id = ((Number) mapa.get("id")).intValue();
                Map<?, ?> clienteJson = (Map<?, ?>) mapa.get("cliente");
                int clienteId = ((Number) clienteJson.get("id")).intValue();
                Cliente cliente = clientesPorId.getOrDefault(clienteId,
                        new Cliente(clienteId, (String) clienteJson.get("nome"), (String) clienteJson.get("email")));

                Pedido pedido = new Pedido(id, cliente);
                pedido.setStatus(StatusPedido.valueOf(((String) mapa.get("status")).toUpperCase()));

                Object itensObj = mapa.get("itens");
                if (itensObj instanceof List<?> itens) {
                    for (Object itemObj : itens) {
                        if (!(itemObj instanceof Map<?, ?> itemMapa)) continue;
                        Map<?, ?> produtoMapa = (Map<?, ?>) itemMapa.get("produto");
                        int produtoId = ((Number) produtoMapa.get("id")).intValue();
                        Produto produto = produtosPorId.getOrDefault(produtoId,
                                new Produto(produtoId,
                                        (String) produtoMapa.get("nome"),
                                        ((Number) produtoMapa.get("preco")).doubleValue(),
                                        Categoria.valueOf(((String) produtoMapa.get("categoria")).toUpperCase())));
                        int quantidade = ((Number) itemMapa.get("quantidade")).intValue();
                        pedido.adicionarItem(produto, quantidade);
                    }
                }
                pedidos.add(pedido);
            }
            return pedidos;
        }

        /**
         * Parser simples para transformar texto JSON em estruturas de mapas/listas
         * consumidas pelos métodos de conversão.
         */
        /**
         * Parser JSON minimalista baseado em ponteiro de caracteres, suficiente para
         * o escopo deste projeto (objetos, arrays, strings, números, boolean e null).
         */
        private static class Parser {
            private final String json;
            private int pos;

            Parser(String json) {
                this.json = json;
            }

            /** Decide qual tipo JSON ler com base no próximo caractere. */
            Object parseValue() {
                skipWhitespace();
                if (pos >= json.length()) {
                    throw new IllegalArgumentException("JSON inesperadamente vazio");
                }
                char c = json.charAt(pos);
                return switch (c) {
                    case '{' -> parseObject();
                    case '[' -> parseArray();
                    case '"' -> parseString();
                    case 't', 'f' -> parseBoolean();
                    case 'n' -> parseNull();
                    default -> parseNumber();
                };
            }

            /** Lê um objeto JSON no formato { "chave": valor, ... }. */
            private Map<String, Object> parseObject() {
                Map<String, Object> mapa = new HashMap<>();
                pos++; // '{'
                skipWhitespace();
                if (peek('}')) {
                    pos++;
                    return mapa;
                }
                while (true) {
                    skipWhitespace();
                    String chave = parseString();
                    skipWhitespace();
                    expect(':');
                    Object valor = parseValue();
                    mapa.put(chave, valor);
                    skipWhitespace();
                    if (peek('}')) {
                        pos++;
                        break;
                    }
                    expect(',');
                }
                return mapa;
            }

            /** Lê um array JSON no formato [ valor, ... ]. */
            private List<Object> parseArray() {
                List<Object> lista = new ArrayList<>();
                pos++; // '['
                skipWhitespace();
                if (peek(']')) {
                    pos++;
                    return lista;
                }
                while (true) {
                    Object valor = parseValue();
                    lista.add(valor);
                    skipWhitespace();
                    if (peek(']')) {
                        pos++;
                        break;
                    }
                    expect(',');
                }
                return lista;
            }

            /** Lê uma string JSON, tratando escapes padrão e unicode. */
            private String parseString() {
                expect('"');
                StringBuilder sb = new StringBuilder();
                while (pos < json.length()) {
                    char c = json.charAt(pos++);
                    if (c == '"') {
                        break;
                    }
                    if (c == '\\') {
                        if (pos >= json.length()) throw new IllegalArgumentException("Escape inválido");
                        char esc = json.charAt(pos++);
                        switch (esc) {
                            case '"', '\\', '/' -> sb.append(esc);
                            case 'b' -> sb.append('\b');
                            case 'f' -> sb.append('\f');
                            case 'n' -> sb.append('\n');
                            case 'r' -> sb.append('\r');
                            case 't' -> sb.append('\t');
                            case 'u' -> {
                                if (pos + 4 > json.length()) throw new IllegalArgumentException("Escape unicode inválido");
                                String hex = json.substring(pos, pos + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 4;
                            }
                            default -> throw new IllegalArgumentException("Escape inválido: \\" + esc);
                        }
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            }

            /** Lê um número JSON (inteiro ou decimal com expoente). */
            private Number parseNumber() {
                int inicio = pos;
                if (peek('-')) pos++;
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
                if (peek('.')) {
                    pos++;
                    while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
                }
                if (peek('e') || peek('E')) {
                    pos++;
                    if (peek('+') || peek('-')) pos++;
                    while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
                }
                String numero = json.substring(inicio, pos);
                if (numero.contains(".") || numero.contains("e") || numero.contains("E")) {
                    return Double.parseDouble(numero);
                }
                long valorLongo = Long.parseLong(numero);
                if (valorLongo <= Integer.MAX_VALUE && valorLongo >= Integer.MIN_VALUE) {
                    return (int) valorLongo;
                }
                return valorLongo;
            }

            /** Lê os literais true/false. */
            private Boolean parseBoolean() {
                if (json.startsWith("true", pos)) {
                    pos += 4;
                    return true;
                }
                if (json.startsWith("false", pos)) {
                    pos += 5;
                    return false;
                }
                throw new IllegalArgumentException("Valor booleano inválido");
            }

            /** Lê o literal null. */
            private Object parseNull() {
                if (json.startsWith("null", pos)) {
                    pos += 4;
                    return null;
                }
                throw new IllegalArgumentException("Valor nulo inválido");
            }

            /** Avança o cursor ignorando espaços em branco. */
            private void skipWhitespace() {
                while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
            }

            /** Verifica se o próximo caractere é igual ao esperado (sem consumir). */
            private boolean peek(char esperado) {
                return pos < json.length() && json.charAt(pos) == esperado;
            }

            /** Garante que o próximo caractere seja o esperado (consome-o). */
            private void expect(char esperado) {
                skipWhitespace();
                if (pos >= json.length() || json.charAt(pos) != esperado) {
                    throw new IllegalArgumentException("Esperado '" + esperado + "' em " + pos);
                }
                pos++;
                skipWhitespace();
            }
        }
    }
}

