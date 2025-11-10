package app; // Diz que este arquivo faz parte do pacote "app" (como uma pasta organizada de arquivos Java)

import java.io.*; // Importa ferramentas para ler e escrever arquivos (usado para salvar os dados)
import java.util.*; // Importa listas, mapas e o Scanner (para entrada do usuário)

/**
 * Classe principal (ponto de entrada do programa).
 * Esse programa é um sistema de pedidos que roda no console.
 * Permite cadastrar clientes, produtos, criar pedidos e salvar tudo em arquivos JSON.
 */
public class Main {

    // Essas três listas guardam os dados em memória enquanto o programa está aberto
    private static List<Cliente> clientes = new ArrayList<>(); // lista com todos os clientes
    private static List<Produto> produtos = new ArrayList<>(); // lista com todos os produtos
    private static List<Pedido> pedidos = new ArrayList<>();   // lista com todos os pedidos

    // Esse objeto é responsável por "processar" os pedidos em segundo plano (thread separada)
    private static PedidoProcessor processor = new PedidoProcessor();

    /**
     * Função principal (onde o programa começa).
     * Aqui o sistema inicia a thread, carrega dados salvos e mostra o menu principal.
     */
    public static void main(String[] args) {
        new Thread(processor).start(); // Inicia a thread que processa os pedidos
        carregarDados(); // Tenta carregar dados salvos de arquivos (clientes, produtos, pedidos)

        Scanner sc = new Scanner(System.in); // Cria o Scanner, usado para ler o que o usuário digita no console

        // Esse laço "while(true)" mantém o menu aparecendo até o usuário escolher sair
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

            int opcao = lerInteiro(sc); // Lê a opção digitada pelo usuário e garante que é um número

            // Usa "switch" para decidir o que fazer de acordo com a escolha do usuário
            switch (opcao) {
                case 1 -> cadastrarCliente(sc); // Chama a função que cadastra um cliente
                case 2 -> cadastrarProduto(sc); // Chama a função que cadastra um produto
                case 3 -> criarPedido(sc);      // Chama a função que cria um pedido
                case 4 -> listarClientes();     // Mostra todos os clientes
                case 5 -> listarProdutos();     // Mostra todos os produtos
                case 6 -> listarPedidos();      // Mostra todos os pedidos feitos
                case 7 -> {                     // Opção de salvar e sair do programa
                    salvarDados();              // Salva todos os dados nos arquivos
                    System.out.println("Até logo!");
                    System.exit(0);             // Encerra o programa
                }
                default -> System.out.println("Opção inválida!"); // Caso o usuário digite algo errado
            }
        }
    }

    /**
     * Cadastra um novo cliente pedindo nome e e-mail.
     * Faz validação (por exemplo, não deixar nome vazio) e adiciona o cliente à lista.
     */
    private static void cadastrarCliente(Scanner sc) {
        System.out.print("Nome: ");
        String nome = sc.nextLine(); // Lê o nome digitado
        System.out.print("E-mail: ");
        String email = sc.nextLine(); // Lê o e-mail digitado

        try {
            // Cria um novo objeto Cliente com ID automático
            Cliente c = new Cliente(gerarNovoId(clientes), nome, email);
            clientes.add(c); // Adiciona o cliente na lista
            System.out.println("Cliente cadastrado!");
        } catch (ValidacaoException e) {
            // Caso o cliente seja inválido (nome vazio, e-mail incorreto, etc.)
            System.out.println("Falha ao cadastrar cliente: " + e.getMessage());
        }
    }

    /**
     * Cadastra um novo produto pedindo nome, preço e categoria.
     */
    private static void cadastrarProduto(Scanner sc) {
        System.out.print("Nome: ");
        String nome = sc.nextLine(); // Nome do produto
        System.out.print("Preço: ");
        double preco = lerDouble(sc); // Lê o preço e garante que é número
        System.out.print("Categoria (ALIMENTOS, ELETRONICOS, LIVROS): ");

        try {
            // Converte o texto digitado para uma categoria válida (enum)
            Categoria cat = Categoria.valueOf(sc.nextLine().trim().toUpperCase());
            // Cria o produto e adiciona à lista
            Produto p = new Produto(gerarNovoId(produtos), nome, preco, cat);
            produtos.add(p);
            System.out.println("Produto cadastrado!");
        } catch (IllegalArgumentException e) {
            // Categoria digitada incorretamente
            System.out.println("Categoria inválida.");
        } catch (ValidacaoException e) {
            // Caso o produto tenha preço inválido ou outro problema
            System.out.println("Falha ao cadastrar produto: " + e.getMessage());
        }
    }

    /**
     * Cria um novo pedido, vinculado a um cliente existente.
     * Permite adicionar vários produtos até o usuário digitar 0.
     */
    private static void criarPedido(Scanner sc) {
        // Se não existir cliente ou produto, não dá pra criar pedido
        if (clientes.isEmpty() || produtos.isEmpty()) {
            System.out.println("Cadastre clientes e produtos antes!");
            return; // Sai da função
        }

        listarClientes(); // Mostra todos os clientes
        System.out.print("ID do Cliente: ");
        int id = lerInteiro(sc); // Lê o ID escolhido

        // Procura o cliente com o ID digitado
        Cliente cliente = clientes.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        if (cliente == null) {
            System.out.println("Cliente não encontrado!");
            return;
        }

        // Cria o pedido para esse cliente
        Pedido pedido = new Pedido(gerarNovoId(pedidos), cliente);

        // Laço para adicionar itens ao pedido
        while (true) {
            listarProdutos(); // Mostra produtos disponíveis
            System.out.print("ID do Produto (0 para finalizar): ");
            int pid = lerInteiro(sc); // Lê o ID do produto

            if (pid == 0) break; // Digitar 0 significa "parar de adicionar itens"

            // Procura o produto pelo ID
            Produto prod = produtos.stream().filter(p -> p.getId() == pid).findFirst().orElse(null);
            if (prod == null) {
                System.out.println("Produto não encontrado!");
                continue; // Volta para o início do laço
            }

            System.out.print("Quantidade: ");
            int quantidade = lerInteiro(sc); // Lê a quantidade
            if (quantidade <= 0) {
                System.out.println("Quantidade inválida.");
                continue;
            }

            try {
                // Adiciona o item no pedido
                pedido.adicionarItem(prod, quantidade);
            } catch (ValidacaoException e) {
                System.out.println("Falha ao adicionar item: " + e.getMessage());
            }
        }

        // Se o pedido estiver vazio, não deixa criar
        if (pedido.getItens().isEmpty()) {
            System.out.println("Pedido precisa ter pelo menos um item.");
            return;
        }

        // Adiciona o pedido à lista geral e manda para processamento
        pedidos.add(pedido);
        processor.adicionarPedido(pedido);
        System.out.println("Pedido criado e adicionado à fila!");
    }

    /**
     * Mostra todos os clientes cadastrados na tela.
     */
    private static void listarClientes() {
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        // Para cada cliente, mostra suas informações
        clientes.forEach(cliente ->
            System.out.println("Cliente [ID=" + cliente.getId() +
                               ", Nome=" + cliente.getNome() +
                               ", Email=" + cliente.getEmail() + "]"));
    }

    /**
     * Mostra todos os produtos cadastrados.
     */
    private static void listarProdutos() {
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        produtos.forEach(produto ->
            System.out.println("Produto [ID=" + produto.getId() +
                               ", Nome=" + produto.getNome() +
                               ", Preço=" + produto.getPreco() +
                               ", Categoria=" + produto.getCategoria() + "]"));
    }

    /**
     * Mostra todos os pedidos com seus itens e status.
     */
    private static void listarPedidos() {
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido cadastrado.");
            return;
        }

        // Para cada pedido, mostra os detalhes e os itens
        pedidos.forEach(pedido -> {
            System.out.println(pedido); // Mostra informações básicas do pedido
            pedido.getItens().forEach(item ->
                System.out.println("  - " + item.getQuantidade() + "x " +
                                   item.getProduto().getNome() +
                                   " (" + item.calcularSubtotal() + ")"));
        });
    }

    /**
     * Lê um número inteiro digitado pelo usuário.
     * Se o usuário digitar algo errado, o programa pede novamente até conseguir um número válido.
     */
    private static int lerInteiro(Scanner sc) {
        while (true) {
            try {
                int valor = Integer.parseInt(sc.nextLine().trim()); // Tenta converter o texto para número
                return valor;
            } catch (NumberFormatException e) {
                // Se o usuário digitar algo que não é número, mostra mensagem e tenta de novo
                System.out.print("Valor inválido, tente novamente: ");
            }
        }
    }

    // OBS: As funções "lerDouble", "salvarDados", "carregarDados" e "gerarNovoId"
    // devem estar em outro trecho do código (outra parte da classe)
    // e cuidam de ler números decimais, salvar os arquivos e criar IDs automáticos.



    /**
     * Lê um número decimal do console, aceitando vírgula ou ponto como separador.
    /**
     * Função para ler um número decimal (ex: 12.50) digitado pelo usuário.
     * Se o usuário digitar algo errado (como letras), o programa pede novamente.
     */
    private static double lerDouble(Scanner sc) {
        while (true) { // repete até o usuário digitar um número válido
            try {
                // Lê o texto digitado, troca vírgula por ponto (caso o usuário digite "12,50")
                // e tenta converter para número decimal (double)
                double valor = Double.parseDouble(sc.nextLine().replace(',', '.').trim());
                return valor; // se der certo, devolve o número e sai da função
            } catch (NumberFormatException e) {
                // Caso o texto não seja número (ex: "abc"), mostra mensagem e tenta de novo
                System.out.print("Valor inválido, tente novamente: ");
            }
        }
    }

    /**
     * Gera automaticamente um novo ID (número único) para qualquer tipo de objeto que tenha um getId().
     * Exemplo: se o último cliente tem ID 3, o próximo terá ID 4.
     */
    private static <T extends Identificavel> int gerarNovoId(List<T> itens) {
        // Pega todos os IDs da lista, acha o maior e soma +1
        // Se a lista estiver vazia, começa do 1 (pois orElse(0) + 1 = 1)
        return itens.stream().mapToInt(Identificavel::getId).max().orElse(0) + 1;
    }

    // Nome do arquivo onde o sistema vai salvar e carregar os dados
    private static final String ARQUIVO_DADOS = "dados.json";

    /**
     * Salva todos os dados atuais (clientes, produtos e pedidos) dentro de um arquivo JSON.
     * Isso serve para que, quando o programa for fechado, os dados não se percam.
     */
    private static void salvarDados() {
        // Usa "try-with-resources" para abrir o arquivo e garantir que ele será fechado corretamente depois
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_DADOS))) {
            // Cria um objeto que vai guardar todas as listas do sistema
            DadosPersistidos dados = new DadosPersistidos();
            dados.clientes = clientes;
            dados.produtos = produtos;
            dados.pedidos = pedidos;

            // Converte os dados em texto JSON usando o JsonUtil
            writer.write(JsonUtil.toJson(dados));
            System.out.println("Dados salvos em dados.json");
        } catch (IOException e) {
            // Se algo der errado (ex: não consegue escrever no arquivo), mostra erro
            System.out.println("Falha ao salvar dados: " + e.getMessage());
        }
    }

    /**
     * Lê o arquivo "dados.json" e recria na memória todas as listas (clientes, produtos e pedidos).
     * Assim, o programa continua do ponto em que parou na última vez.
     */
    private static void carregarDados() {
        File arquivo = new File(ARQUIVO_DADOS); // Cria uma "referência" para o arquivo dados.json

        // Se o arquivo ainda não existe, é a primeira vez que o programa está rodando
        if (!arquivo.exists()) {
            System.out.println("Nenhum dado anterior encontrado.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            StringBuilder conteudo = new StringBuilder();
            String linha;

            // Lê o arquivo linha por linha e monta o texto completo do JSON
            while ((linha = reader.readLine()) != null) {
                conteudo.append(linha).append('\n');
            }

            // Converte o texto JSON de volta para objetos Java (listas de clientes, produtos e pedidos)
            DadosPersistidos dados = JsonUtil.fromJson(conteudo.toString());

            // Se não conseguir interpretar o JSON (arquivo vazio ou corrompido)
            if (dados == null) {
                System.out.println("Nenhum dado anterior encontrado.");
                return;
            }

            // Limpa as listas antigas e substitui pelos dados carregados
            clientes.clear();
            produtos.clear();
            pedidos.clear();

            if (dados.clientes != null) {
                clientes.addAll(dados.clientes);
            }
            if (dados.produtos != null) {
                produtos.addAll(dados.produtos);
            }

            // Cria um mapa para ligar o ID de cada produto ao objeto real
            // (isso ajuda a atualizar os produtos dentro dos pedidos)
            Map<Integer, Produto> produtosPorId = new HashMap<>();
            produtos.forEach(produto -> produtosPorId.put(produto.getId(), produto));

            if (dados.pedidos != null) {
                for (Pedido pedido : dados.pedidos) {
                    pedidos.add(pedido); // adiciona o pedido na lista principal

                    // Atualiza a referência de cada produto dentro dos itens do pedido
                    if (pedido.getItens() != null) {
                        pedido.getItens().forEach(item -> {
                            Produto produto = produtosPorId.get(item.getProduto().getId());
                            if (produto != null) item.atualizarProduto(produto);
                        });
                    }
                }
            }

            // Recoloca na fila de processamento todos os pedidos que estavam pendentes
            pedidos.stream()
                   .filter(p -> p.getStatus() == StatusPedido.FILA || p.getStatus() == StatusPedido.PROCESSANDO)
                   .forEach(processor::adicionarPedido);

            System.out.println("Dados carregados!");
        } catch (RuntimeException e) {
            // Caso o arquivo tenha um conteúdo que não segue o formato JSON esperado
            System.out.println("Conteúdo de dados.json inválido. Um novo arquivo será gerado ao salvar.");
        } catch (IOException e) {
            // Caso não consiga abrir ou ler o arquivo
            System.out.println("Falha ao carregar dados: " + e.getMessage());
        }
    }

    /**
     * Classe interna usada para agrupar todos os dados que serão salvos no arquivo JSON.
     * É como uma "caixa" que guarda listas de clientes, produtos e pedidos.
     */
    private static class DadosPersistidos {
        List<Cliente> clientes;
        List<Produto> produtos;
        List<Pedido> pedidos;
    }

    /**
     * Classe responsável por converter os dados do sistema para JSON e o contrário (JSON → objetos).
     * Faz isso manualmente, sem depender de bibliotecas externas como Gson ou Jackson.
     */
    private static class JsonUtil {

        /**
         * Transforma as listas de clientes, produtos e pedidos em texto JSON.
         * Usa StringBuilder para montar o texto de forma organizada e com identação.
         */
        private static String toJson(DadosPersistidos dados) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n"); // começa o objeto JSON
            appendClientes(sb, dados.clientes, 1); // escreve a parte dos clientes
            sb.append(",\n");
            appendProdutos(sb, dados.produtos, 1); // escreve a parte dos produtos
            sb.append(",\n");
            appendPedidos(sb, dados.pedidos, 1);   // escreve a parte dos pedidos
            sb.append('\n').append('}'); // fecha o JSON
            return sb.toString(); // devolve o texto pronto
        }

        /**
         * Faz o caminho inverso: lê um texto JSON e recria os objetos (clientes, produtos e pedidos).
         */
        private static DadosPersistidos fromJson(String json) {
            // Se o texto estiver vazio, não há o que carregar
            if (json == null || json.trim().isEmpty()) {
                return null;
            }

            // Usa o Parser interno para interpretar o texto JSON em estruturas básicas (Mapas e Listas)
            Object parsed = new Parser(json).parseValue();

            // Se o resultado não for um "objeto JSON", lança erro
            if (!(parsed instanceof Map<?, ?> mapa)) {
                throw new IllegalArgumentException("JSON deve representar um objeto");
            }

            // Cria o contêiner e reconstrói as listas de entidades (clientes, produtos e pedidos)
            DadosPersistidos dados = new DadosPersistidos();
            dados.clientes = parseClientes(obterLista(mapa, "clientes"));
            dados.produtos = parseProdutos(obterLista(mapa, "produtos"));
            dados.pedidos = parsePedidos(obterLista(mapa, "pedidos"), dados.clientes, dados.produtos);
            return dados;
        }

        /** Escreve a parte dos clientes no JSON. */
        private static void appendClientes(StringBuilder sb, List<Cliente> clientes, int nivel) {
            indent(sb, nivel).append("\"clientes\": ["); // abre o campo "clientes"

            // Se houver clientes cadastrados, escreve um por um
            if (clientes != null && !clientes.isEmpty()) {
                sb.append('\n');
                for (int i = 0; i < clientes.size(); i++) {
                    Cliente c = clientes.get(i);
                    indent(sb, nivel + 1).append('{').append('\n');
                    indent(sb, nivel + 2).append("\"id\": ").append(c.getId()).append(',').append('\n');
                    indent(sb, nivel + 2).append("\"nome\": \"").append(escapar(c.getNome())).append("\",").append('\n');
                    indent(sb, nivel + 2).append("\"email\": \"").append(escapar(c.getEmail())).append("\"").append('\n');
                    indent(sb, nivel + 1).append('}');
                    if (i < clientes.size() - 1) sb.append(','); // adiciona vírgula se não for o último
                    sb.append('\n');
                }
                indent(sb, nivel).append(']');
            } else {
                sb.append(']'); // caso não tenha clientes, só fecha o colchete
            }
        }

        /** Escreve a parte dos produtos no JSON. */
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


        /** 
         * Escreve a lista de pedidos no formato JSON.
         * Aqui, cada pedido é convertido em texto, mostrando o cliente, 
         * os itens comprados e o status do pedido.
         */
        private static void appendPedidos(StringBuilder sb, List<Pedido> pedidos, int nivel) {
            // Começa o campo "pedidos" no JSON
            indent(sb, nivel).append("\"pedidos\": [");
            
            // Verifica se há pedidos para salvar
            if (pedidos != null && !pedidos.isEmpty()) {
                sb.append('\n');
                
                // Percorre cada pedido da lista
                for (int i = 0; i < pedidos.size(); i++) {
                    Pedido pedido = pedidos.get(i);

                    // Abre um novo objeto JSON para o pedido
                    indent(sb, nivel + 1).append('{').append('\n');
                    indent(sb, nivel + 2).append("\"id\": ").append(pedido.getId()).append(',').append('\n');

                    // Adiciona os dados do cliente dentro do pedido
                    indent(sb, nivel + 2).append("\"cliente\": {").append('\n');
                    indent(sb, nivel + 3).append("\"id\": ").append(pedido.getCliente().getId()).append(',').append('\n');
                    indent(sb, nivel + 3).append("\"nome\": \"").append(escapar(pedido.getCliente().getNome())).append("\",").append('\n');
                    indent(sb, nivel + 3).append("\"email\": \"").append(escapar(pedido.getCliente().getEmail())).append("\"").append('\n');
                    indent(sb, nivel + 2).append("},\n");

                    // Adiciona os itens comprados dentro do pedido
                    indent(sb, nivel + 2).append("\"itens\": [");
                    if (!pedido.getItens().isEmpty()) {
                        sb.append('\n');
                        
                        // Para cada item dentro do pedido
                        for (int j = 0; j < pedido.getItens().size(); j++) {
                            ItemPedido item = pedido.getItens().get(j);

                            indent(sb, nivel + 3).append('{').append('\n');
                            // Adiciona informações do produto dentro do item
                            indent(sb, nivel + 4).append("\"produto\": {").append('\n');
                            indent(sb, nivel + 5).append("\"id\": ").append(item.getProduto().getId()).append(',').append('\n');
                            indent(sb, nivel + 5).append("\"nome\": \"").append(escapar(item.getProduto().getNome())).append("\",").append('\n');
                            indent(sb, nivel + 5).append("\"preco\": ").append(item.getProduto().getPreco()).append(',').append('\n');
                            indent(sb, nivel + 5).append("\"categoria\": \"").append(item.getProduto().getCategoria()).append("\"").append('\n');
                            indent(sb, nivel + 4).append("},\n");

                            // Adiciona a quantidade do produto comprada
                            indent(sb, nivel + 4).append("\"quantidade\": ").append(item.getQuantidade()).append('\n');
                            indent(sb, nivel + 3).append('}');
                            if (j < pedido.getItens().size() - 1) sb.append(','); // Adiciona vírgula se não for o último item
                            sb.append('\n');
                        }
                        indent(sb, nivel + 2).append(']');
                    } else {
                        sb.append(']');
                    }

                    // Adiciona o status (FILA, PROCESSANDO, FINALIZADO)
                    sb.append(',').append('\n');
                    indent(sb, nivel + 2).append("\"status\": \"").append(pedido.getStatus()).append("\"").append('\n');
                    indent(sb, nivel + 1).append('}');
                    if (i < pedidos.size() - 1) sb.append(','); // vírgula entre pedidos
                    sb.append('\n');
                }
                indent(sb, nivel).append(']');
            } else {
                sb.append(']');
            }
        }

        /** 
         * Adiciona espaços em branco para "indentar" (deixar o JSON bonito e organizado).
         * Cada nível aumenta a quantidade de espaços antes das linhas.
         */
        private static StringBuilder indent(StringBuilder sb, int nivel) {
            for (int i = 0; i < nivel; i++) {
                sb.append("  "); // dois espaços por nível
            }
            return sb;
        }

        /** 
         * Garante que o texto não tenha caracteres que possam quebrar o formato JSON.
         * Exemplo: aspas dentro de nomes, barras, ou quebras de linha.
         */
        private static String escapar(String valor) {
            if (valor == null) return "";
            StringBuilder sb = new StringBuilder();
            for (char c : valor.toCharArray()) {
                switch (c) {
                    case '\\' -> sb.append("\\\\"); // barra invertida vira \\
                    case '"' -> sb.append("\\\"");   // aspas duplas viram \"
                    case '\n' -> sb.append("\\n");   // quebra de linha vira \n
                    case '\r' -> sb.append("\\r");   // retorno de carro vira \r
                    case '\t' -> sb.append("\\t");   // tabulação vira \t
                    default -> sb.append(c);         // outros caracteres ficam iguais
                }
            }
            return sb.toString();
        }

        /** 
         * Busca uma lista (array) dentro do JSON.
         * Se o campo não existir ou não for uma lista, cria uma lista vazia ou lança erro.
         */
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

        /** 
         * Converte uma lista "crua" (vinda do JSON) em objetos Cliente de verdade.
         * Ou seja, pega os dados e transforma em instâncias da classe Cliente.
         */
        private static List<Cliente> parseClientes(List<?> lista) {
            List<Cliente> clientes = new ArrayList<>();
            for (Object obj : lista) {
                if (!(obj instanceof Map<?, ?> mapa)) continue; // ignora se não for um objeto
                int id = ((Number) mapa.get("id")).intValue();
                String nome = (String) mapa.get("nome");
                String email = (String) mapa.get("email");
                clientes.add(new Cliente(id, nome, email)); // cria novo cliente
            }
            return clientes;
        }

        /** 
         * Converte uma lista "crua" (vinda do JSON) em objetos Produto.
         * Assim, os produtos voltam a ser objetos da aplicação.
         */
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
         * Converte a lista de pedidos vinda do JSON em objetos Pedido completos,
         * com cliente, produtos e itens. 
         * Também liga os pedidos aos objetos já existentes de Cliente e Produto.
         */
        private static List<Pedido> parsePedidos(List<?> lista, List<Cliente> clientes, List<Produto> produtos) {
            // Cria mapas para achar clientes e produtos pelo ID
            Map<Integer, Cliente> clientesPorId = new HashMap<>();
            for (Cliente cliente : clientes) {
                clientesPorId.put(cliente.getId(), cliente);
            }

            Map<Integer, Produto> produtosPorId = new HashMap<>();
            for (Produto produto : produtos) {
                produtosPorId.put(produto.getId(), produto);
            }

            List<Pedido> pedidos = new ArrayList<>();

            // Percorre cada pedido vindo do JSON
            for (Object obj : lista) {
                if (!(obj instanceof Map<?, ?> mapa)) continue;
                int id = ((Number) mapa.get("id")).intValue();

                // Lê os dados do cliente dentro do pedido
                Map<?, ?> clienteJson = (Map<?, ?>) mapa.get("cliente");
                int clienteId = ((Number) clienteJson.get("id")).intValue();

                // Tenta achar o cliente existente pelo ID, senão cria um novo
                Cliente cliente = clientesPorId.getOrDefault(clienteId,
                        new Cliente(clienteId, (String) clienteJson.get("nome"), (String) clienteJson.get("email")));

                // Cria o pedido com o cliente
                Pedido pedido = new Pedido(id, cliente);

                // Define o status (ex: FILA, PROCESSANDO, FINALIZADO)
                pedido.setStatus(StatusPedido.valueOf(((String) mapa.get("status")).toUpperCase()));

                // Lê os itens do pedido
                Object itensObj = mapa.get("itens");
                if (itensObj instanceof List<?> itens) {
                    for (Object itemObj : itens) {
                        if (!(itemObj instanceof Map<?, ?> itemMapa)) continue;
                        Map<?, ?> produtoMapa = (Map<?, ?>) itemMapa.get("produto");
                        int produtoId = ((Number) produtoMapa.get("id")).intValue();

                        // Recupera o produto pelo ID, ou cria um novo se não achar
                        Produto produto = produtosPorId.getOrDefault(produtoId,
                                new Produto(produtoId,
                                        (String) produtoMapa.get("nome"),
                                        ((Number) produtoMapa.get("preco")).doubleValue(),
                                        Categoria.valueOf(((String) produtoMapa.get("categoria")).toUpperCase())));

                        // Lê a quantidade do item e adiciona ao pedido
                        int quantidade = ((Number) itemMapa.get("quantidade")).intValue();
                        pedido.adicionarItem(produto, quantidade);
                    }
                }
                pedidos.add(pedido); // adiciona o pedido completo à lista
            }
            return pedidos;
        }

        /**
         * Classe interna responsável por interpretar texto JSON e convertê-lo
         * em estruturas Java como Map (para objetos) e List (para arrays).
         * 
         * Esse parser é "manual": percorre o texto caractere por caractere,
         * interpretando tipos básicos (objetos, arrays, strings, números, booleanos e null).
         */
        private static class Parser {
            private final String json; // Texto JSON completo a ser analisado
            private int pos;           // Índice atual do "cursor" no texto

            // Construtor que recebe o texto JSON
            Parser(String json) {
                this.json = json;
            }

            /**
             * Método principal do parser: identifica o tipo do próximo valor JSON
             * com base no primeiro caractere e chama o método apropriado.
             */
            Object parseValue() {
                skipWhitespace(); // Ignora espaços em branco antes do valor
                if (pos >= json.length()) {
                    throw new IllegalArgumentException("JSON inesperadamente vazio");
                }
                char c = json.charAt(pos);
                // Decide o tipo de valor pelo primeiro caractere
                return switch (c) {
                    case '{' -> parseObject();  // Objeto
                    case '[' -> parseArray();   // Array
                    case '"' -> parseString();  // String
                    case 't', 'f' -> parseBoolean(); // true/false
                    case 'n' -> parseNull();    // null
                    default -> parseNumber();   // Número
                };
            }

            /**
             * Lê um objeto JSON no formato: { "chave": valor, "outra": valor }
             * Retorna um Map<String, Object> com as chaves e valores.
             */
            private Map<String, Object> parseObject() {
                Map<String, Object> mapa = new HashMap<>();
                pos++; // Avança além da chave '{'
                skipWhitespace();

                // Caso o objeto esteja vazio, como {}
                if (peek('}')) {
                    pos++;
                    return mapa;
                }

                // Loop até encontrar o final do objeto '}'
                while (true) {
                    skipWhitespace();
                    String chave = parseString(); // Lê a chave (sempre string)
                    skipWhitespace();
                    expect(':'); // Espera o caractere ':'
                    Object valor = parseValue(); // Lê o valor da chave
                    mapa.put(chave, valor); // Adiciona no mapa

                    skipWhitespace();
                    // Se o próximo caractere for '}', fecha o objeto
                    if (peek('}')) {
                        pos++;
                        break;
                    }
                    // Caso contrário, espera uma vírgula ',' entre os pares
                    expect(',');
                }
                return mapa;
            }

            /**
             * Lê um array JSON no formato: [ valor, valor, ... ]
             * Retorna uma lista com os valores lidos.
             */
            private List<Object> parseArray() {
                List<Object> lista = new ArrayList<>();
                pos++; // Avança além do '['
                skipWhitespace();

                // Caso o array esteja vazio, como []
                if (peek(']')) {
                    pos++;
                    return lista;
                }

                // Loop até encontrar o final do array ']'
                while (true) {
                    Object valor = parseValue(); // Lê o próximo valor
                    lista.add(valor);
                    skipWhitespace();

                    if (peek(']')) { // Se o array terminou
                        pos++;
                        break;
                    }
                    expect(','); // Espera uma vírgula entre os valores
                }
                return lista;
            }

            /**
             * Lê uma string JSON, tratando caracteres de escape e Unicode.
             * Exemplo: "Olá\nMundo" ou "Caminho\\arquivo.txt"
             */
            private String parseString() {
                expect('"'); // Garante que a string começa com aspas
                StringBuilder sb = new StringBuilder();

                while (pos < json.length()) {
                    char c = json.charAt(pos++);
                    if (c == '"') {
                        break; // Fim da string
                    }

                    // Trata caracteres de escape como \" ou \n
                    if (c == '\\') {
                        if (pos >= json.length())
                            throw new IllegalArgumentException("Escape inválido");

                        char esc = json.charAt(pos++);
                        switch (esc) {
                            case '"', '\\', '/' -> sb.append(esc);   // Aspas, barra ou barra invertida
                            case 'b' -> sb.append('\b');             // Backspace
                            case 'f' -> sb.append('\f');             // Form feed
                            case 'n' -> sb.append('\n');             // Nova linha
                            case 'r' -> sb.append('\r');             // Retorno de carro
                            case 't' -> sb.append('\t');             // Tabulação
                            case 'u' -> {                            // Unicode (\)
                                if (pos + 4 > json.length())
                                    throw new IllegalArgumentException("Escape unicode inválido");
                                String hex = json.substring(pos, pos + 4); // Lê 4 dígitos hexadecimais
                                sb.append((char) Integer.parseInt(hex, 16)); // Converte pra caractere
                                pos += 4;
                            }
                            default -> throw new IllegalArgumentException("Escape inválido: \\" + esc);
                        }
                    } else {
                        sb.append(c); // Caractere comum
                    }
                }
                return sb.toString();
            }

            /**
             * Lê um número JSON.
             * Pode ser inteiro (ex: 42) ou decimal (ex: 3.14), com expoente opcional (ex: 1.2e10).
             */
            private Number parseNumber() {
                int inicio = pos;

                // Sinal negativo opcional
                if (peek('-')) pos++;

                // Parte inteira
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;

                // Parte decimal opcional
                if (peek('.')) {
                    pos++;
                    while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
                }

                // Expoente opcional (e/E seguido de dígitos)
                if (peek('e') || peek('E')) {
                    pos++;
                    if (peek('+') || peek('-')) pos++; // sinal do expoente
                    while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
                }

                // Pega o número completo como substring
                String numero = json.substring(inicio, pos);

                // Se tiver ponto ou expoente, converte para Double
                if (numero.contains(".") || numero.contains("e") || numero.contains("E")) {
                    return Double.parseDouble(numero);
                }

                // Caso contrário, tenta converter para inteiro (int ou long)
                long valorLongo = Long.parseLong(numero);
                if (valorLongo <= Integer.MAX_VALUE && valorLongo >= Integer.MIN_VALUE) {
                    return (int) valorLongo;
                }
                return valorLongo;
            }

            /**
             * Lê valores booleanos (true/false).
             */
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

            /**
             * Lê o valor literal null.
             */
            private Object parseNull() {
                if (json.startsWith("null", pos)) {
                    pos += 4;
                    return null;
                }
                throw new IllegalArgumentException("Valor nulo inválido");
            }

            /**
             * Pula todos os caracteres de espaço, quebra de linha, etc.
             * antes de ler o próximo valor.
             */
            private void skipWhitespace() {
                while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
            }

            /**
             * Verifica se o próximo caractere é o esperado,
             * sem consumir (usado para checar fim de objeto/array).
             */
            private boolean peek(char esperado) {
                return pos < json.length() && json.charAt(pos) == esperado;
            }

            /**
             * Garante que o próximo caractere seja o esperado.
             * Se for, consome-o; se não, lança um erro.
             * 
             * Exemplo: expect(':') garante que o próximo símbolo é ':'
             */
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
