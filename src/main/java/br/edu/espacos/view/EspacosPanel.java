package br.edu.espacos.view;

import br.edu.espacos.client.EspacosClient;
import br.edu.espacos.model.TipoEspaco;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class EspacosPanel extends JPanel {
    private EspacosClient client;
    private String tipoUsuario;
    private String usuarioId;

    private JTable tabelaEspacos;
    private DefaultTableModel modeloEspacos;

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EspacosPanel(EspacosClient client, String tipoUsuario, String usuarioId) {
        this.client = client;
        this.tipoUsuario = tipoUsuario;
        this.usuarioId = usuarioId;
        setLayout(new BorderLayout());
        initComponents();
        setupLayout();
        atualizarEspacos();
    }

    private void initComponents() {
        String[] colunasEspacos = {"ID", "Nome", "Tipo", "Localização", "Capacidade"};
        modeloEspacos = new DefaultTableModel(colunasEspacos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaEspacos = new JTable(modeloEspacos);
        tabelaEspacos.getColumnModel().getColumn(0).setMinWidth(0);
        tabelaEspacos.getColumnModel().getColumn(0).setMaxWidth(0);
        tabelaEspacos.getColumnModel().getColumn(0).setWidth(0);
    }

    private void setupLayout() {
        add(new JScrollPane(tabelaEspacos), BorderLayout.CENTER);

        JPanel botoesEspacos = new JPanel(new FlowLayout());
        JButton btnNovaReserva = new JButton("Nova Reserva");
        JButton btnMinhasReservas = new JButton("Minhas Reservas");
        botoesEspacos.add(btnNovaReserva);
        botoesEspacos.add(btnMinhasReservas);

        JButton btnVerDetalhesEspaco = new JButton("Ver Detalhes do Espaço");
        botoesEspacos.add(btnVerDetalhesEspaco);
        btnVerDetalhesEspaco.addActionListener(e -> mostrarDetalhesEspacoSelecionado());

        if ("ADMIN".equals(tipoUsuario)) {
            JButton btnCadastrarEspaco = new JButton("Cadastrar Espaço");
            JButton btnRemoverEspaco = new JButton("Remover Espaço");
            botoesEspacos.add(btnCadastrarEspaco);
            botoesEspacos.add(btnRemoverEspaco);
            btnCadastrarEspaco.addActionListener(e -> mostrarCadastroEspaco());
            btnRemoverEspaco.addActionListener(e -> removerEspacoSelecionado());
        }

        add(botoesEspacos, BorderLayout.SOUTH);

        btnNovaReserva.addActionListener(e -> mostrarNovaReserva());
        btnMinhasReservas.addActionListener(e -> mostrarMinhasReservas());
    }

    public void atualizarEspacos() {
        int linhaSelecionada = tabelaEspacos.getSelectedRow();
        String nomeEspacoSelecionado = null;
        if (linhaSelecionada != -1) {
            nomeEspacoSelecionado = (String) modeloEspacos.getValueAt(linhaSelecionada, 1);
        }

        String resposta = client.listarEspacos();
        String[] partes = resposta.split("\\|");

        modeloEspacos.setRowCount(0);

        if (partes[0].equals("ESPACOS") && partes.length > 1 && !partes[1].trim().isEmpty()) {
            String[] espacos = partes[1].split(":");
            for (String espaco : espacos) {
                if (!espaco.trim().isEmpty()) {
                    String[] dados = espaco.split(";");
                    if (dados.length >= 5) {
                        modeloEspacos.addRow(new Object[]{
                                dados[0], dados[1], dados[2], dados[3], dados[4]
                        });
                    }
                }
            }
        }

        if (nomeEspacoSelecionado != null) {
            for (int i = 0; i < modeloEspacos.getRowCount(); i++) {
                if (nomeEspacoSelecionado.equals(modeloEspacos.getValueAt(i, 1))) {
                    tabelaEspacos.setRowSelectionInterval(i, i);
                    tabelaEspacos.scrollRectToVisible(tabelaEspacos.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }

    private void mostrarDetalhesEspacoSelecionado() {
        int linhaSelecionada = tabelaEspacos.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um espaço para ver os detalhes.");
            return;
        }

        String espacoId = (String) modeloEspacos.getValueAt(linhaSelecionada, 0);
        String tipoEspaco = (String) modeloEspacos.getValueAt(linhaSelecionada, 2);

        String resposta = client.buscarDetalhesEspaco(espacoId, tipoEspaco);
        String[] partes = resposta.split("\\|");

        if (partes[0].equals("DETALHES_ESPACO")) {
            StringBuilder detalhes = new StringBuilder();
            detalhes.append("Detalhes do Espaço: ").append(modeloEspacos.getValueAt(linhaSelecionada, 1)).append("\n");
            detalhes.append("---------------------------------------\n");
            detalhes.append("ID: ").append(modeloEspacos.getValueAt(linhaSelecionada, 0)).append("\n");
            detalhes.append("Nome: ").append(modeloEspacos.getValueAt(linhaSelecionada, 1)).append("\n");
            detalhes.append("Tipo: ").append(modeloEspacos.getValueAt(linhaSelecionada, 2)).append("\n");
            detalhes.append("Localização: ").append(modeloEspacos.getValueAt(linhaSelecionada, 3)).append("\n");
            detalhes.append("Capacidade: ").append(modeloEspacos.getValueAt(linhaSelecionada, 4)).append("\n");

            if (partes.length > 1 && !partes[1].isEmpty()) {
                detalhes.append("\nAtributos Específicos:\n");
                String[] atributos = partes[1].split(";");
                for (String atributo : atributos) {
                    detalhes.append("- ").append(atributo).append("\n");
                }
            }

            JTextArea textArea = new JTextArea(detalhes.toString());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane, "Detalhes do Espaço", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao buscar detalhes do espaço: " + resposta, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerEspacoSelecionado() {
        int linhaSelecionada = tabelaEspacos.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um espaço para remover.");
            return;
        }

        String espacoId = (String) modeloEspacos.getValueAt(linhaSelecionada, 0);
        String nomeEspaco = (String) modeloEspacos.getValueAt(linhaSelecionada, 1);

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja remover o espaço '" + nomeEspaco + "'?",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            String resposta = client.removerEspaco(espacoId);
            if (resposta.startsWith("REMOVER_ESPACO_OK")) {
                JOptionPane.showMessageDialog(this, "Espaço removido com sucesso!");
                atualizarEspacos();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao remover espaço: " + resposta, "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarCadastroEspaco() {
        CadastroEspacoDialog dialog = new CadastroEspacoDialog(client);
        dialog.setVisible(true);
        if (dialog.wasSuccessful()) {
            atualizarEspacos();
        }
    }

    private void mostrarNovaReserva() {
        int linhaSelecionada = tabelaEspacos.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um espaço primeiro.");
            return;
        }
        String espacoId = (String) modeloEspacos.getValueAt(linhaSelecionada, 0);
        String nomeEspaco = (String) modeloEspacos.getValueAt(linhaSelecionada, 1);
        NovaReservaDialog dialog = new NovaReservaDialog(client, espacoId, nomeEspaco, usuarioId);
        dialog.setVisible(true);
    }

    private void mostrarMinhasReservas() {
        MinhasReservasDialog dialog = new MinhasReservasDialog(client, usuarioId);
        dialog.setVisible(true);
    }
}