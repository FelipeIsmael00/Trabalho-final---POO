package app;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Consumidor assíncrono de pedidos que processa a fila e atualiza seus status
 * conforme o fluxo definido.
 */
public class PedidoProcessor implements Runnable {
    /**
     * Fila concorrente (thread-safe) responsável por armazenar pedidos aguardando
     * processamento. A implementação {@link LinkedBlockingQueue} garante
     * sincronização interna, permitindo múltiplos produtores/consumidores sem
     * necessidade de bloqueios explícitos.
     */
    private BlockingQueue<Pedido> fila = new LinkedBlockingQueue<>();

    /**
     * Enfileira um novo pedido definindo seu status como {@link StatusPedido#FILA}.
     */
    public void adicionarPedido(Pedido pedido) {
        pedido.setStatus(StatusPedido.FILA);
        fila.add(pedido);
    }

    @Override
    public void run() {
        // Loop de vida da thread. O método take() bloqueia enquanto a fila estiver
        // vazia, reduzindo consumo de CPU e despertando automaticamente quando um
        // novo pedido é enfileirado.
        while (true) {
            try {
                Pedido pedido = fila.take();

                // Transição de estado: FILA -> PROCESSANDO
                pedido.setStatus(StatusPedido.PROCESSANDO);
                System.out.println("Processando pedido " + pedido.getId() + "...");

                // Simula trabalho pesado (ex.: integração com pagamento/estoque)
                Thread.sleep(3000);

                // Transição de estado: PROCESSANDO -> FINALIZADO
                pedido.setStatus(StatusPedido.FINALIZADO);
                System.out.println("Pedido " + pedido.getId() + " finalizado!");
            } catch (InterruptedException e) {
                // Interrupção sinaliza encerramento gracioso da thread
                break;
            }
        }
    }
}
