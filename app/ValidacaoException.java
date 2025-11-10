package app;

/**
 * Exceção de domínio lançada quando regras de validação são violadas.
 */
public class ValidacaoException extends RuntimeException {
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}

