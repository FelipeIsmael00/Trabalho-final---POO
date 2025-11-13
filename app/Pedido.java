// Indica que esta classe faz parte do pacote "app" (como uma pasta que organiza os arquivos)
package app;

// Importa classes da biblioteca padrão do Java usadas aqui
import java.util.ArrayList;     // Para criar listas que podem crescer dinamicamente
import java.util.Collections;   // Para utilitários de coleções (como listas de leitura apenas)
import java.util.List;          // Tipo genérico de lista

// Declara uma classe pública chamada Pedido
// "implements Identificavel" significa que ela segue um contrato que exige um método getId()
public class Pedido implements Identificavel {
    
    // Atributos (características) do pedido:
    private int id;                          // Identificador numérico do pedido
    private Cliente cliente;                 // O cliente que fez o pedido
    private List<ItemPedido> itens = new ArrayList<>(); // Lista de itens do pedido
    private StatusPedido status = StatusPedido.ABERTO;  // Situação atual do pedido (começa como ABERTO)

    // Construtor: é chamado quando criamos um novo Pedido (new Pedido(...))
    // Recebe o id e o cliente como parâmetros obrigatórios
    public Pedido(int id, Cliente cliente) {
        validar(id, cliente); // Chama o método que checa se os dados são válidos
        this.id = id;         // "this.id" é o atributo da classe, recebe o valor de "id" passado
        this.cliente = cliente; // Atribui o cliente informado
    }

    // Construtor protegido, usado por sistemas de serialização (quando o Java cria objetos automaticamente)
    protected Pedido() {
        // Construtor padrão para serialização
    }

    // Método privado que valida se os dados são corretos antes de criar o pedido
    private void validar(int id, Cliente cliente) {
        // Se o id for menor ou igual a zero, lança um erro
        if (id <= 0) throw new ValidacaoException("ID do pedido inválido");
        // Se o cliente for nulo (não informado), lança outro erro
        if (cliente == null) throw new ValidacaoException("Pedido precisa de um cliente");
    }

    // O método abaixo vem da interface "Identificavel"
    // Retorna o ID do pedido (serve para identificar cada um)
    @Override
    public int getId() { 
        return id; 
    }

    // Retorna o cliente do pedido
    public Cliente getCliente() { 
        return cliente; 
    }

    // Retorna a lista de itens, mas de forma "somente leitura"
    // Assim ninguém de fora pode alterar a lista diretamente
    public List<ItemPedido> getItens() { 
        return Collections.unmodifiableList(itens); 
    }

    // Retorna o status atual do pedido (ABERTO, PROCESSANDO, CONCLUIDO, etc)
    public StatusPedido getStatus() { 
        return status; 
    }

    // Permite alterar o status do pedido
    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    // Método para adicionar um item novo ao pedido a partir de um produto e quantidade
    public void adicionarItem(Produto produto, int quantidade) {
        // Cria um novo objeto ItemPedido com o produto e a quantidade
        // e adiciona esse objeto à lista de itens
        itens.add(new ItemPedido(produto, quantidade));
    }

    // Outra forma de adicionar um item: passando o ItemPedido já pronto
    public void adicionarItem(ItemPedido item) {
        // Verifica se o item é nulo (vazio ou inexistente)
        if (item == null) throw new ValidacaoException("Item inválido");
        // Se for válido, adiciona à lista
        itens.add(item);
    }

    // Calcula o total do pedido somando o subtotal de cada item
    public double calcularTotal() {
        // "stream()" cria um fluxo de dados dos itens
        // "mapToDouble" pega o subtotal de cada item (preço * quantidade)
        // "sum()" soma todos os valores
        return itens.stream().mapToDouble(ItemPedido::calcularSubtotal).sum();
    }

    // Método especial que transforma o objeto em texto
    // Útil para imprimir ou mostrar informações do pedido
   @Override
    public String toString() {
        return "Pedido [ID=" + id + ", Cliente=" + cliente.getNome() + ", Total=" + calcularTotal() + ", Status=" + status + "]";
    }
}
