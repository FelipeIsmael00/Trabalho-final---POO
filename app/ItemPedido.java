package app;

/**
 * Item pertencente a um pedido, combinando produto e quantidade.
 */
public class ItemPedido {
    private Produto produto;
    private int quantidade;

    /**
     * Cria um item validando produto associado e quantidade positiva.
     */
    public ItemPedido(Produto produto, int quantidade) {
        validar(produto, quantidade);
        this.produto = produto;
        this.quantidade = quantidade;
    }

    protected ItemPedido() {
        // Construtor padrão para serialização
    }

    private void validar(Produto produto, int quantidade) {
        if (produto == null) throw new ValidacaoException("Produto do item obrigatório");
        if (quantidade <= 0) throw new ValidacaoException("Quantidade deve ser positiva");
    }

    public Produto getProduto() {
        return produto;
    }

    /**
     * Atualiza a referência do produto após carregamento de dados.
     */
    public void atualizarProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double calcularSubtotal() {
        return quantidade * produto.getPreco();
    }
}

