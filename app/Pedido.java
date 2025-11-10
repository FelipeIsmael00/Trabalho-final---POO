package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido implements Identificavel {
    private int id;
    private Cliente cliente;
    private List<ItemPedido> itens = new ArrayList<>();
    private StatusPedido status = StatusPedido.ABERTO;

    public Pedido(int id, Cliente cliente) {
        validar(id, cliente);
        this.id = id;
        this.cliente = cliente;
    }

    protected Pedido() {
        // Construtor padrão para serialização
    }

    private void validar(int id, Cliente cliente) {
        if (id <= 0) throw new ValidacaoException("ID do pedido inválido");
        if (cliente == null) throw new ValidacaoException("Pedido precisa de um cliente");
    }

    @Override
    public int getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
    public StatusPedido getStatus() { return status; }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public void adicionarItem(Produto produto, int quantidade) {
        itens.add(new ItemPedido(produto, quantidade));
    }

    public void adicionarItem(ItemPedido item) {
        if (item == null) throw new ValidacaoException("Item inválido");
        itens.add(item);
    }

    public double calcularTotal() {
        return itens.stream().mapToDouble(ItemPedido::calcularSubtotal).sum();
    }

    @Override
    public String toString() {
        return "Pedido [ID=" + id + ", Cliente=" + cliente.getNome() + ", Total=" + calcularTotal() + ", Status=" + status + "]";
    }
}
