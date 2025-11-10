package app;

/**
 * Representa um cliente do sistema com validações básicas de identificação.
 */
public class Cliente implements Identificavel {
    private int id;
    private String nome;
    private String email;

    /**
     * Cria um cliente validando ID, nome e e-mail.
     */
    public Cliente(int id, String nome, String email) {
        validar(id, nome, email);
        this.id = id;
        this.nome = nome.trim();
        this.email = email.trim();
    }

    protected Cliente() {
        // Construtor padrão necessário para serialização
    }

    private void validar(int id, String nome, String email) {
        if (id <= 0) throw new ValidacaoException("ID do cliente inválido");
        if (nome == null || nome.isBlank()) throw new ValidacaoException("Nome do cliente obrigatório");
        if (email == null || email.isBlank()) throw new ValidacaoException("E-mail do cliente obrigatório");
        if (!email.contains("@")) throw new ValidacaoException("E-mail do cliente inválido");
    }

    @Override
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return "Cliente [ID=" + id + ", Nome=" + nome + ", Email=" + email + "]";
    }
}
