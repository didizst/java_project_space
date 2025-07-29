package br.edu.espacos.view;

import br.edu.espacos.client.EspacosClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MinhasReservasDialog extends JDialog {
    private EspacosClient client;
    private String usuarioId;
    private JTable tabelaMinhasReservas;
    private DefaultTableModel modeloMinhasReservas;
    private Timer timer;

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MinhasReservasDialog(EspacosClient client, String usuarioId) {
        this.client = client;
        this.usuarioId = usuarioId;
        setTitle("Minhas Reservas");
        setSize(700, 500);
        setModal(true);
        setLayout(new BorderLayout());
        initComponents();
        setupLayout();
        setLocationRelativeTo(null); // Centraliza o diálogo
    }

    private void initComponents() {
        String[] colunas = {"ID Reserva", "Espaço", "Tipo", "Início", "Fim", "Status", "Finalidade"};
        modeloMinhasReservas = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaMinhasReservas = new JTable(modeloMinhasReservas);
    }

    private void setupLayout() {
        add(new JScrollPane(tabelaMinhasReservas), BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout());
        JButton btnCancelarReserva = new JButton("Cancelar Reserva");
        JButton btnExportarReservas = new JButton("Exportar Minhas Reservas CSV");
        JButton btnExportarComprovante = new JButton("Exportar Comprovante");
        botoes.add(btnCancelarReserva);
        botoes.add(btnExportarReservas);
        botoes.add(btnExportarComprovante);
        add(botoes, BorderLayout.SOUTH);

        btnExportarComprovante.addActionListener(e -> exportarComprovanteReserva());
        btnCancelarReserva.addActionListener(e -> cancelarReserva());
        btnExportarReservas.addActionListener(e -> exportarMinhasReservasCSV());

        timer = new Timer(2000, e -> atualizarMinhasReservas());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                timer.start();
                atualizarMinhasReservas();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                timer.stop();
            }
        });
    }

    private void exportarComprovanteReserva() {
        int linhaSelecionada = tabelaMinhasReservas.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma reserva para exportar o comprovante.");
            return;
        }

        String reservaId = (String) modeloMinhasReservas.getValueAt(linhaSelecionada, 0);
        String nomeEspaco = (String) modeloMinhasReservas.getValueAt(linhaSelecionada, 1);
        String dataHoraInicio = (String) modeloMinhasReservas.getValueAt(linhaSelecionada, 3);
        String dataHoraFim = (String) modeloMinhasReservas.getValueAt(linhaSelecionada, 4);
        String status = (String) modeloMinhasReservas.getValueAt(linhaSelecionada, 5);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Comprovante de Reserva");
        fileChooser.setSelectedFile(new File("comprovante_reserva_" + reservaId + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                writer.write("=== COMPROVANTE DE RESERVA ===\n");
                writer.write("\n");
                writer.write("ID da Reserva: " + reservaId + "\n");
                writer.write("Espaço: " + nomeEspaco + "\n");
                writer.write("Início: " + dataHoraInicio + "\n");
                writer.write("Fim: " + dataHoraFim + "\n");
                writer.write("Status: " + status + "\n");
                writer.write("Usuário: " + usuarioId + "\n");
                writer.write("Data de Emissão: " + LocalDateTime.now().format(DISPLAY_FORMATTER) + "\n");
                writer.write("\n");
                writer.write("==============================\n");

                JOptionPane.showMessageDialog(this, "Comprovante exportado com sucesso para: " + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao exportar comprovante: " + ex.getMessage(),
                        "Erro de Exportação",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelarReserva() {
        int linhaSelecionada = tabelaMinhasReservas.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma reserva para cancelar.");
            return;
        }

        String idReserva = (String) modeloMinhasReservas.getValueAt(linhaSelecionada, 0);
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja cancelar a reserva " + idReserva + "?",
                "Confirmar Cancelamento",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            String resposta = client.cancelarReserva(idReserva);
            if (resposta.startsWith("SUCESSO")) {
                JOptionPane.showMessageDialog(this, "Reserva cancelada com sucesso!");
                atualizarMinhasReservas();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao cancelar reserva: " + resposta, "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void atualizarMinhasReservas() {
        int linhaSelecionada = tabelaMinhasReservas.getSelectedRow();
        String reservaIdSelecionada = null;
        if (linhaSelecionada != -1) {
            reservaIdSelecionada = (String) modeloMinhasReservas.getValueAt(linhaSelecionada, 0);
        }

        String resposta = client.listarMinhasReservas(usuarioId);
        String[] partes = resposta.split("\\|");

        modeloMinhasReservas.setRowCount(0);

        if (partes[0].equals("MINHAS_RESERVAS") && partes.length > 1 && !partes[1].trim().isEmpty()) {
            String[] reservas = partes[1].split("###");
            for (String reserva : reservas) {
                if (!reserva.trim().isEmpty()) {
                    String[] dados = reserva.split(";");
                    if (dados.length >= 7) {
                        try {
                            LocalDateTime inicio = LocalDateTime.parse(dados[3], INPUT_FORMATTER);
                            LocalDateTime fim = LocalDateTime.parse(dados[4], INPUT_FORMATTER);
                            modeloMinhasReservas.addRow(new Object[]{
                                    dados[0], // ID Reserva
                                    dados[1], // Espaço
                                    dados[2], // Tipo
                                    inicio.format(DISPLAY_FORMATTER),
                                    fim.format(DISPLAY_FORMATTER),
                                    dados[5], // Status
                                    dados[6]  // Finalidade
                            });
                        } catch (DateTimeParseException e) {
                            modeloMinhasReservas.addRow(new Object[]{
                                    dados[0], dados[1], dados[2],
                                    "Data Inválida",
                                    "Data Inválida",
                                    dados[5], dados[6]
                            });
                        }
                    }
                }
            }
        }

        if (reservaIdSelecionada != null) {
            for (int i = 0; i < modeloMinhasReservas.getRowCount(); i++) {
                String reservaId = (String) modeloMinhasReservas.getValueAt(i, 0);
                if (reservaIdSelecionada.equals(reservaId)) {
                    tabelaMinhasReservas.setRowSelectionInterval(i, i);
                    tabelaMinhasReservas.scrollRectToVisible(tabelaMinhasReservas.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }

    private void exportarMinhasReservasCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Minhas Reservas CSV");
        fileChooser.setSelectedFile(new File("minhas_reservas.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                for (int i = 0; i < modeloMinhasReservas.getColumnCount(); i++) {
                    writer.write("\"" + modeloMinhasReservas.getColumnName(i) + "\"");
                    if (i < modeloMinhasReservas.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");

                for (int i = 0; i < modeloMinhasReservas.getRowCount(); i++) {
                    for (int j = 0; j < modeloMinhasReservas.getColumnCount(); j++) {
                        writer.write("\"" + modeloMinhasReservas.getValueAt(i, j).toString().replaceAll("\"", "\"\"") + "\"");
                        if (j < modeloMinhasReservas.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }

                JOptionPane.showMessageDialog(this, "Minhas reservas exportadas com sucesso para: " + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao exportar reservas: " + ex.getMessage(),
                        "Erro de Exportação",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}