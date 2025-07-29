package br.edu.espacos.view;

import br.edu.espacos.client.EspacosClient;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RelatoriosPanel extends JPanel {
    private EspacosClient client;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RelatoriosPanel(EspacosClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JTextArea areaRelatorio = new JTextArea();
        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel botoesRelatorio = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGerarRelatorio = new JButton("Mostrar Relatório");
        JButton btnExportarRelatorioCSV = new JButton("Exportar Relatório CSV");
        JButton btnExportarTodasReservas = new JButton("Exportar Todas as Reservas");
        botoesRelatorio.add(btnGerarRelatorio);
        botoesRelatorio.add(btnExportarRelatorioCSV);
        botoesRelatorio.add(btnExportarTodasReservas);

        btnGerarRelatorio.addActionListener(e -> {
            String resposta = client.gerarRelatorio();
            String[] partes = resposta.split("\\|");

            if (partes[0].equals("RELATORIO")) {
                StringBuilder relatorio = new StringBuilder();
                relatorio.append("=== RELATÓRIO DO SISTEMA ===\n\n");

                for (int i = 1; i < partes.length; i++) {
                    relatorio.append(partes[i]).append("\n");
                }

                relatorio.append("\nGerado em: ").append(LocalDateTime.now().format(DISPLAY_FORMATTER));

                areaRelatorio.setText(relatorio.toString());
            } else {
                areaRelatorio.setText("Erro ao gerar relatório: " + resposta);
            }
        });

        btnExportarRelatorioCSV.addActionListener(e -> exportarRelatorioCSV(areaRelatorio.getText()));
        btnExportarTodasReservas.addActionListener(e -> exportarTodasReservasCSV());

        add(new JScrollPane(areaRelatorio), BorderLayout.CENTER);
        add(botoesRelatorio, BorderLayout.SOUTH);
    }

    private void exportarRelatorioCSV(String conteudoRelatorio) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Relatório CSV");
        fileChooser.setSelectedFile(new File("relatorio_espacos.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                writer.write("Tipo,Valor\n");

                String[] linhas = conteudoRelatorio.split("\n");
                for (String linha : linhas) {
                    if (linha.contains(":")) {
                        String[] partes = linha.split(":");
                        if (partes.length == 2) {
                            String tipo = partes[0].trim().replaceAll(",", "");
                            String valor = partes[1].trim().replaceAll(",", "");
                            writer.write(String.format("\"%s\",\"%s\"\n", tipo, valor));
                        }
                    }
                }

                JOptionPane.showMessageDialog(this, "Relatório exportado com sucesso para: " + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao exportar relatório: " + ex.getMessage(),
                        "Erro de Exportação",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportarTodasReservasCSV() {
        String resposta = client.listarTodasReservas();
        String[] partes = resposta.split("\\|");

        if (partes[0].equals("TODAS_RESERVAS") && partes.length > 1 && !partes[1].trim().isEmpty()) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Exportar Todas as Reservas CSV");
            fileChooser.setSelectedFile(new File("todas_reservas.csv"));

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                    writer.write("ID Reserva,Espaço,Usuário,Início,Fim,Finalidade,Observações,Status\n");

                    String[] reservas = partes[1].split("@@@");
                    for (String reserva : reservas) {
                        if (!reserva.trim().isEmpty()) {
                            String[] dados = reserva.split("###");
                            if (dados.length >= 8) {
                                LocalDateTime inicio = LocalDateTime.parse(dados[3], INPUT_FORMATTER);
                                LocalDateTime fim = LocalDateTime.parse(dados[4], INPUT_FORMATTER);
                                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                                        dados[0],
                                        dados[1],
                                        dados[2],
                                        inicio.format(CSV_DATE_FORMATTER),
                                        fim.format(CSV_DATE_FORMATTER),
                                        dados[5],
                                        dados[6],
                                        dados[7]
                                ));
                            }
                        }
                    }

                    JOptionPane.showMessageDialog(this, "Todas as reservas exportadas com sucesso para: " + fileToSave.getAbsolutePath());
                } catch (IOException | DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao exportar todas as reservas: " + ex.getMessage(),
                            "Erro de Exportação",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nenhuma reserva para exportar ou erro ao listar reservas.", "Informação", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}