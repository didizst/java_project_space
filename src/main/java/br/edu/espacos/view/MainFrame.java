package br.edu.espacos.view;

import br.edu.espacos.client.EspacosClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private EspacosClient client;
    private String nomeUsuario;
    private String tipoUsuario;
    private String usuarioId;

    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    private Timer timerEspacos;

    public MainFrame(EspacosClient client, String nomeUsuario, String tipoUsuario, String usuarioId) {
        this.client = client;
        this.nomeUsuario = nomeUsuario;
        this.tipoUsuario = tipoUsuario;
        this.usuarioId = usuarioId;

        setTitle("Sistema de Gestão de Espaços Acadêmicos");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        initComponents();
        setupLayout();
        setupEventListeners();

        iniciarTimers();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        statusLabel = new JLabel("Conectado como: " + nomeUsuario + " (" + tipoUsuario + ")");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        EspacosPanel espacosPanel = new EspacosPanel(client, tipoUsuario, usuarioId);
        tabbedPane.addTab("Espaços", espacosPanel);

        if ("ADMIN".equals(tipoUsuario)) {
            RelatoriosPanel relatoriosPanel = new RelatoriosPanel(client);
            tabbedPane.addTab("Relatórios", relatoriosPanel);

            UsuariosPanel usuariosPanel = new UsuariosPanel(client);
            tabbedPane.addTab("Usuários", usuariosPanel);
        }

        add(tabbedPane, BorderLayout.CENTER);

        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.add(statusLabel, BorderLayout.WEST);

        JButton btnDeslogar = new JButton("Deslogar");
        btnDeslogar.setBackground(new Color(220, 53, 69));
        btnDeslogar.setForeground(Color.BLACK);
        btnDeslogar.setFocusPainted(false);
        btnDeslogar.addActionListener(e -> deslogar());
        painelInferior.add(btnDeslogar, BorderLayout.EAST);

        add(painelInferior, BorderLayout.SOUTH);
    }

    private void iniciarTimers() {
        timerEspacos = new Timer(2000, e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                Component comp = tabbedPane.getComponentAt(0);
                if (comp instanceof EspacosPanel) {
                    ((EspacosPanel) comp).atualizarEspacos();
                }
            }
        });
        timerEspacos.start();
    }

    private void setupEventListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                deslogar();
            }
        });
    }

    private void deslogar() {
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja sair do sistema?",
                "Confirmar Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            timerEspacos.stop();
            client.desconectar();
            dispose();
            System.exit(0);
        }
    }
}