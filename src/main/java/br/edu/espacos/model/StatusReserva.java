package br.edu.espacos.model;

/**
 * Enum que define os status possíveis de uma reserva
 */
public enum StatusReserva {
    PENDENTE("Pendente"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada"),
    CONCLUIDA("Concluída");

    private final String descricao;

    StatusReserva(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}


