package br.edu.espacos.view;

import br.edu.espacos.client.EspacosClient;
import br.edu.espacos.model.TipoUsuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UsuariosPanel extends JPanel {
    private EspacosClient client;
    private DefaultTableModel modeloUsuarios;
    private JTable tabelaUsuarios;

    public UsuariosPanel(EspacosClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        String[] colunasUsuarios = {"ID", "Nome", "Email", "Tipo", "Ativo"};
        modeloUsuarios = new DefaultTableModel(colunasUsuarios, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Boolean.class;
                return String.class;
            }
        };

        tabelaUsuarios = new JTable(modeloUsuarios);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnDeletar = new JButton("Deletar Usuário");
        JButton btnToggleAdmin = new JButton("Promover/Rebaixar");
        JButton btnRegistrar = new JButton("Registrar Novo Usuário");

        btnDeletar.setBackground(new Color(220, 53, 69));
        btnDeletar.setForeground(Color.BLACK);
        btnToggleAdmin.setBackground(new Color(40, 167, 69));
        btnToggleAdmin.setForeground(Color.BLACK);
        btnRegistrar.setBackground(new Color(23, 162, 184));
        btnRegistrar.setForeground(Color.BLACK);

        btnAtualizar.addActionListener(e -> atualizarTabelaUsuarios());
        btnDeletar.addActionListener(e -> deletarUsuario());
        btnToggleAdmin.addActionListener(e -> toggleAdminUsuario());
        btnRegistrar.addActionListener(e -> registrarNovoUsuario());

        botoesPanel.add(btnAtualizar);
        botoesPanel.add(btnDeletar);
        botoesPanel.add(btnToggleAdmin);
        botoesPanel.add(btnRegistrar);

        add(new JScrollPane(tabelaUsuarios), BorderLayout.CENTER);
        add(botoesPanel, BorderLayout.SOUTH);

        atualizarTabelaUsuarios();
    }

    private void atualizarTabelaUsuarios() {
        modeloUsuarios.setRowCount(0);
        String resposta = client.listarUsuarios();

        if (resposta.startsWith("USUARIOS|")) {
            String[] partes = resposta.split("\\|");
            if (partes.length > 1 && !partes[1].isEmpty()) {
                String[] usuarios = partes[1].split("@@@");
                for (String usuario : usuarios) {
                    String[] dados = usuario.split(";");
                    if (dados.length >= 5) {
                        modeloUsuarios.addRow(new Object[]{
                                dados[0], // ID
                                dados[1], // Nome
                                dados[2], // Email
                                dados[3], // Tipo
                                Boolean.parseBoolean(dados[4]) // Ativo
                        });
                    }
                }
            }
        }
    }

    private void deletarUsuario() {
        int linha = tabelaUsuarios.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário.");
            return;
        }
        String id = (String) modeloUsuarios.getValueAt(linha, 0);
        String nome = (String) modeloUsuarios.getValueAt(linha, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja deletar o usuário " + nome + "?",
                "Confirmar Deleção",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String resposta = client.deletarUsuario(id);
            if (resposta.startsWith("SUCESSO")) {
                JOptionPane.showMessageDialog(this, "Usuário deletado com sucesso.");
                atualizarTabelaUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao deletar usuário: " + resposta);
            }
        }
    }

    private void toggleAdminUsuario() {
        int linha = tabelaUsuarios.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário.");
            return;
        }
        String id = (String) modeloUsuarios.getValueAt(linha, 0);
        String nome = (String) modeloUsuarios.getValueAt(linha, 1);
        String tipoAtual = (String) modeloUsuarios.getValueAt(linha, 3);

        String novoTipo = tipoAtual.equals("ADMIN") ? "COMUM" : "ADMIN";
        String acao = novoTipo.equals("ADMIN") ? "promover a administrador" : "rebaixar a usuário comum";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja " + acao + " o usuário " + nome + "?",
                "Alterar Tipo de Usuário",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String resposta = client.alterarTipoUsuario(id, novoTipo);
            if (resposta.startsWith("SUCESSO")) {
                JOptionPane.showMessageDialog(this, "Tipo de usuário alterado com sucesso.");
                atualizarTabelaUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao alterar tipo: " + resposta);
            }
        }
    }

    private void registrarNovoUsuario() {
        JDialog registroDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Registrar Novo Usuário", true);
        registroDialog.setSize(400, 350);
        registroDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField nomeField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField senhaField = new JPasswordField(20);
        JPasswordField confirmSenhaField = new JPasswordField(20);
        JComboBox<TipoUsuario> tipoCombo = new JComboBox<>(TipoUsuario.values());

        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nome:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        panel.add(nomeField, gbc);

        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Senha:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        panel.add(senhaField, gbc);

        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Confirmar Senha:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        panel.add(confirmSenhaField, gbc);

        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Tipo:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        panel.add(tipoCombo, gbc);

        JButton registrarBtn = new JButton("Registrar");
        JButton cancelarBtn = new JButton("Cancelar");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(registrarBtn);
        buttonPanel.add(cancelarBtn);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        registrarBtn.addActionListener(e -> {
            String nome = nomeField.getText().trim();
            String email = emailField.getText().trim();
            String senha = new String(senhaField.getPassword());
            String confirmSenha = new String(confirmSenhaField.getPassword());
            TipoUsuario tipo = (TipoUsuario) tipoCombo.getSelectedItem();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(registroDialog, "Preencha todos os campos.");
                return;
            }

            if (!senha.equals(confirmSenha)) {
                JOptionPane.showMessageDialog(registroDialog, "As senhas não coincidem.");
                return;
            }

            if (senha.length() < 4) {
                JOptionPane.showMessageDialog(registroDialog, "A senha deve ter pelo menos 4 caracteres.");
                return;
            }

            String resposta = client.registrar(nome, email, senha, tipo.name());
            if (resposta.startsWith("REGISTRO_OK")) {
                JOptionPane.showMessageDialog(registroDialog, "Usuário registrado com sucesso!");
                registroDialog.dispose();
                atualizarTabelaUsuarios();
            } else {
                JOptionPane.showMessageDialog(registroDialog, "Erro ao registrar: " + resposta.split("\\|")[1]);
            }
        });

        cancelarBtn.addActionListener(e -> registroDialog.dispose());

        registroDialog.add(panel);
        registroDialog.setVisible(true);
    }
}