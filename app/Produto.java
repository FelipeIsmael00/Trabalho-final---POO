package app;

/**
 * Entidade que representa um produto comercializado, incluindo categoria e preço
 * validado.
 */
public class Produto implements Identificavel {
    private int id;
    private String nome;
    private double preco;
    private Categoria categoria;

    /**
     * Construtor principal que aplica validações sobre ID, nome, preço e
     * categoria.
     */
    public Produto(int id, String nome, double preco, Categoria categoria) {
        validar(id, nome, preco, categoria);
        this.id = id;
        this.nome = nome.trim();
        this.preco = preco;
        this.categoria = categoria;
    }

    protected Produto() {
        // Construtor padrão para serialização
    }

    private void validar(int id, String nome, double preco, Categoria categoria) {
        if (id <= 0) throw new ValidacaoException("ID do produto inválido");
        if (nome == null || nome.isBlank()) throw new ValidacaoException("Nome do produto obrigatório");
        if (preco <= 0) throw new ValidacaoException("Preço deve ser positivo");
        if (categoria == null) throw new ValidacaoException("Categoria do produto obrigatória");
    }

    @Override
    public int getId() { return id; }
    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public Categoria getCategoria() { return categoria; }

    @Override
    public String toString() {
        return "Produto [ID=" + id + ", Nome=" + nome + ", Preço=" + preco + ", Categoria=" + categoria + "]";
    }
}
