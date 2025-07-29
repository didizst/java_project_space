package br.edu.espacos.view;

import br.edu.espacos.client.EspacosClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class NovaReservaDialog extends JDialog {
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private EspacosClient client;
    private String espacoId;
    private String usuarioId;
    private JTextField finalidadeField;
    private JTextArea observacoesArea;
    private JSpinner dataInicioSpinner;
    private JSpinner dataFimSpinner;

    public NovaReservaDialog(EspacosClient client, String espacoId, String nomeEspaco, String usuarioId) {
        this.client = client;
        this.espacoId = espacoId;
        this.usuarioId = usuarioId;
        setTitle("Nova Reserva");
        setSize(400, 400);
        setModal(true);
        setLocationRelativeTo(null);
        initComponents(nomeEspaco);
    }

    private void initComponents(String nomeEspaco) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        LocalDateTime agora = LocalDateTime.now().plusDays(1);

        dataInicioSpinner = criarSpinnerDataHora();
        dataInicioSpinner.setValue(Date.from(agora.atZone(ZoneId.systemDefault()).toInstant()));

        dataFimSpinner = criarSpinnerDataHora();
        dataFimSpinner.setValue(Date.from(agora.plusHours(1).atZone(ZoneId.systemDefault()).toInstant()));

        finalidadeField = new JTextField(20);
        observacoesArea = new JTextArea(3, 20);
        JScrollPane observacoesScroll = new JScrollPane(observacoesArea);

        JButton btnVerificarDisponibilidade = new JButton("Verificar Disponibilidade");
        btnVerificarDisponibilidade.addActionListener(e -> verificarDisponibilidade());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Espaço:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(nomeEspaco), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Data/Hora Início:"), gbc);
        gbc.gridx = 1;
        panel.add(dataInicioSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Data/Hora Fim:"), gbc);
        gbc.gridx = 1;
        panel.add(dataFimSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Finalidade:"), gbc);
        gbc.gridx = 1;
        panel.add(finalidadeField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Observações:"), gbc);
        gbc.gridx = 1;
        panel.add(observacoesScroll, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(btnVerificarDisponibilidade, gbc);

        JButton btnReservar = new JButton("Reservar");
        JButton btnCancelar = new JButton("Cancelar");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnReservar);
        buttonPanel.add(btnCancelar);

        btnReservar.addActionListener(e -> fazerReserva());
        btnCancelar.addActionListener(e -> dispose());

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        getContentPane().add(panel);
    }

    private void verificarDisponibilidade() {
        Date inicio = (Date) dataInicioSpinner.getValue();
        Date fim = (Date) dataFimSpinner.getValue();

        String inicioStr = LocalDateTime.ofInstant(inicio.toInstant(), ZoneId.systemDefault())
                .format(INPUT_FORMATTER);
        String fimStr = LocalDateTime.ofInstant(fim.toInstant(), ZoneId.systemDefault())
                .format(INPUT_FORMATTER);

        String resposta = client.verificarDisponibilidade(espacoId, inicioStr, fimStr);
        if (resposta.contains("DISPONIVEL")) {
            JOptionPane.showMessageDialog(this, "Horário disponível para reserva!", "Disponível", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Horário indisponível para reserva!", "Conflito", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void fazerReserva() {
        Date inicio = (Date) dataInicioSpinner.getValue();
        Date fim = (Date) dataFimSpinner.getValue();

        String dataInicio = LocalDateTime.ofInstant(inicio.toInstant(), ZoneId.systemDefault())
                .format(INPUT_FORMATTER);
        String dataFim = LocalDateTime.ofInstant(fim.toInstant(), ZoneId.systemDefault())
                .format(INPUT_FORMATTER);

        String finalidade = finalidadeField.getText().trim();
        String observacoes = observacoesArea.getText().trim();

        if (finalidade.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe a finalidade da reserva.");
            return;
        }

        String resposta = client.fazerReserva(espacoId, dataInicio, dataFim, finalidade, observacoes);
        String[] partes = resposta.split("\\|");

        if (partes[0].equals("SUCESSO")) {
            JOptionPane.showMessageDialog(this, "Reserva realizada com sucesso! Código: " + partes[1]);
        } else if (partes[0].equals("RESERVA_ERRO")) {
            JOptionPane.showMessageDialog(this, "Erro ao fazer reserva: " + partes[1], "Erro", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Erro inesperado ao fazer reserva: " + resposta, "Erro", JOptionPane.ERROR_MESSAGE);
        }
        dispose();
    }

    private JSpinner criarSpinnerDataHora() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);

        JSpinner.DateEditor editor = new JSpinner.DateEditor(
                spinner,
                "dd/MM/yyyy HH:mm"
        );
        spinner.setEditor(editor);

        return spinner;
    }
}