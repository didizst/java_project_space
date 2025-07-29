package br.edu.espacos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Representa uma reserva de um espaço.
 */
public class Reserva {
    private String id;
    private String espacoId;
    private String usuarioId;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String finalidade;
    private String observacoes;
    private StatusReserva status;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Reserva(String id, String espacoId, String usuarioId, LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String finalidade, String observacoes) {
        this.id = id;
        this.espacoId = espacoId;
        this.usuarioId = usuarioId;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.finalidade = finalidade;
        this.observacoes = observacoes;
        this.status = StatusReserva.CONFIRMADA; // Status inicial de uma nova reserva
    }

    // Construtor para carregar do arquivo, incluindo o status
    public Reserva(String id, String espacoId, String usuarioId, LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String finalidade, String observacoes, StatusReserva status) {
        this.id = id;
        this.espacoId = espacoId;
        this.usuarioId = usuarioId;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.finalidade = finalidade;
        this.observacoes = observacoes;
        this.status = status;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getEspacoId() {
        return espacoId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public StatusReserva getStatus() {
        return status;
    }

    // Setters
    public void setStatus(StatusReserva status) {
        this.status = status;
    }

    /**
     * Converte o objeto Reserva para uma string formatada para salvar em arquivo.
     * Formato: id|espacoId|usuarioId|dataHoraInicio|dataHoraFim|finalidade|observacoes|status
     */
    public String toFileString() {
        return String.join("|",
                id,
                espacoId,
                usuarioId,
                dataHoraInicio.format(FORMATTER),
                dataHoraFim.format(FORMATTER),
                finalidade,
                observacoes,
                status != null ? status.name() : StatusReserva.PENDENTE.name() // Garante que o status seja salvo
        );
    }

    /**
     * Cria um objeto Reserva a partir de uma string lida do arquivo.
     */
    public static Reserva fromFileString(String fileString) {
        String[] parts = fileString.split("\\|");
        if (parts.length == 8) {
            try {
                String id = parts[0];
                String espacoId = parts[1];
                String usuarioId = parts[2];
                LocalDateTime dataHoraInicio = LocalDateTime.parse(parts[3], FORMATTER);
                LocalDateTime dataHoraFim = LocalDateTime.parse(parts[4], FORMATTER);
                String finalidade = parts[5];
                String observacoes = parts[6];
                StatusReserva status = StatusReserva.valueOf(parts[7]);
                return new Reserva(id, espacoId, usuarioId, dataHoraInicio, dataHoraFim, finalidade, observacoes, status);
            } catch (Exception e) {
                System.err.println("Erro ao parsear linha de reserva: " + fileString + " - " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * Verifica se esta reserva tem conflito de horário com outra reserva.
     */
    public boolean temConflitoCom(Reserva outra) {
        // Se forem a mesma reserva, não há conflito
        if (this.equals(outra)) {
            return false;
        }

        // Conflito se os períodos se sobrepõem
        return (this.dataHoraInicio.isBefore(outra.dataHoraFim) && this.dataHoraFim.isAfter(outra.dataHoraInicio));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id='" + id + '\'' +
                ", espacoId='" + espacoId + '\'' +
                ", usuarioId='" + usuarioId + '\'' +
                ", dataHoraInicio=" + dataHoraInicio +
                ", dataHoraFim=" + dataHoraFim +
                ", finalidade='" + finalidade + '\'' +
                ", observacoes='" + observacoes + '\'' +
                ", status=" + status +
                '}';
    }
}


