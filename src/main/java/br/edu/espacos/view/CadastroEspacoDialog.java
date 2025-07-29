package br.edu.espacos.view;

import br.edu.espacos.client.EspacosClient;
import br.edu.espacos.model.TipoEspaco;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class CadastroEspacoDialog extends JDialog {
    private EspacosClient client;
    private boolean success = false;
    private JPanel atributosEspecificosPanel;
    private JComboBox<TipoEspaco> tipoEspacoComboBox;
    private JTextField nomeField;
    private JTextField localizacaoField;
    private JSpinner capacidadeSpinner;
    private JTextArea descricaoArea;

    public CadastroEspacoDialog(EspacosClient client) {
        this.client = client;
        setTitle("Cadastrar Novo Espaço");
        setSize(500, 600);
        setModal(true);
        setLocationRelativeTo(null);
        initComponents();
    }

    public boolean wasSuccessful() {
        return success;
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nomeField = new JTextField(20);
        localizacaoField = new JTextField(20);
        capacidadeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        descricaoArea = new JTextArea(3, 20);
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);

        tipoEspacoComboBox = new JComboBox<>(TipoEspaco.values());

        atributosEspecificosPanel = new JPanel(new GridBagLayout());
        atributosEspecificosPanel.setBorder(BorderFactory.createTitledBorder("Atributos Específicos"));

        tipoEspacoComboBox.addActionListener(e -> {
            atualizarAtributosEspecificosPanel((TipoEspaco) tipoEspacoComboBox.getSelectedItem());
            panel.revalidate();
            panel.repaint();
        });

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        panel.add(nomeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Localização:"), gbc);
        gbc.gridx = 1;
        panel.add(localizacaoField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Capacidade:"), gbc);
        gbc.gridx = 1;
        panel.add(capacidadeSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descricaoArea), gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Tipo de Espaço:"), gbc);
        gbc.gridx = 1;
        panel.add(tipoEspacoComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(atributosEspecificosPanel, gbc);

        atualizarAtributosEspecificosPanel((TipoEspaco) tipoEspacoComboBox.getSelectedItem());

        JButton btnCadastrar = new JButton("Cadastrar");
        JButton btnCancelar = new JButton("Cancelar");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnCadastrar);
        buttonPanel.add(btnCancelar);

        btnCadastrar.addActionListener(e -> cadastrarEspaco());
        btnCancelar.addActionListener(e -> dispose());

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0; gbc.weighty = 0;
        panel.add(buttonPanel, gbc);

        getContentPane().add(panel);
    }

    private void cadastrarEspaco() {
        String nome = nomeField.getText().trim();
        String localizacao = localizacaoField.getText().trim();
        int capacidade = (int) capacidadeSpinner.getValue();
        String descricao = descricaoArea.getText().trim();
        TipoEspaco tipo = (TipoEspaco) tipoEspacoComboBox.getSelectedItem();

        if (nome.isEmpty() || localizacao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome e Localização são campos obrigatórios.");
            return;
        }

        StringBuilder atributosEspecificos = new StringBuilder();
        switch (Objects.requireNonNull(tipo)) {
            case SALA_AULA:
                JCheckBox temProjetorSalaAula = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temProjetorSalaAula");
                JCheckBox temArCondicionadoSalaAula = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temArCondicionadoSalaAula");
                JCheckBox temQuadroSalaAula = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temQuadroSalaAula");
                JSpinner numeroComputadoresSalaAula = (JSpinner) getComponentByName(atributosEspecificosPanel, "numeroComputadoresSalaAula");
                atributosEspecificos.append(temProjetorSalaAula.isSelected()).append(";");
                atributosEspecificos.append(temArCondicionadoSalaAula.isSelected()).append(";");
                atributosEspecificos.append(temQuadroSalaAula.isSelected()).append(";");
                atributosEspecificos.append(numeroComputadoresSalaAula.getValue());
                break;
            case LABORATORIO:
                JSpinner numeroComputadoresLab = (JSpinner) getComponentByName(atributosEspecificosPanel, "numeroComputadoresLab");
                JTextField tipoLaboratorioField = (JTextField) getComponentByName(atributosEspecificosPanel, "tipoLaboratorioField");
                JCheckBox temProjetorLab = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temProjetorLab");
                JCheckBox temArCondicionadoLab = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temArCondicionadoLab");
                JTextField equipamentosEspeciaisField = (JTextField) getComponentByName(atributosEspecificosPanel, "equipamentosEspeciaisField");
                atributosEspecificos.append(numeroComputadoresLab.getValue()).append(";");
                atributosEspecificos.append(tipoLaboratorioField.getText()).append(";");
                atributosEspecificos.append(temProjetorLab.isSelected()).append(";");
                atributosEspecificos.append(temArCondicionadoLab.isSelected()).append(";");
                atributosEspecificos.append(equipamentosEspeciaisField.getText());
                break;
            case AUDITORIO:
                JCheckBox temProjetorAud = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temProjetorAud");
                JCheckBox temSomProfissionalAud = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temSomProfissionalAud");
                JCheckBox temPalcoAud = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temPalcoAud");
                JCheckBox temArCondicionadoAud = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temArCondicionadoAud");
                JSpinner numeroMicrofonesAud = (JSpinner) getComponentByName(atributosEspecificosPanel, "numeroMicrofonesAud");
                JTextField tipoAssentosAud = (JTextField) getComponentByName(atributosEspecificosPanel, "tipoAssentosAud");
                atributosEspecificos.append(temProjetorAud.isSelected()).append(";");
                atributosEspecificos.append(temSomProfissionalAud.isSelected()).append(";");
                atributosEspecificos.append(temPalcoAud.isSelected()).append(";");
                atributosEspecificos.append(temArCondicionadoAud.isSelected()).append(";");
                atributosEspecificos.append(numeroMicrofonesAud.getValue()).append(";");
                atributosEspecificos.append(tipoAssentosAud.getText());
                break;
            case SALA_REUNIAO:
                JCheckBox temProjetorReuniao = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temProjetorReuniao");
                JCheckBox temArCondicionadoReuniao = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temArCondicionadoReuniao");
                JCheckBox temTelefoneReuniao = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temTelefoneReuniao");
                JCheckBox temVideoconferenciaReuniao = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temVideoconferenciaReuniao");
                JTextField tipoMesaReuniao = (JTextField) getComponentByName(atributosEspecificosPanel, "tipoMesaReuniao");
                atributosEspecificos.append(temProjetorReuniao.isSelected()).append(";");
                atributosEspecificos.append(temArCondicionadoReuniao.isSelected()).append(";");
                atributosEspecificos.append(temTelefoneReuniao.isSelected()).append(";");
                atributosEspecificos.append(temVideoconferenciaReuniao.isSelected()).append(";");
                atributosEspecificos.append(tipoMesaReuniao.getText());
                break;
            case QUADRA_ESPORTIVA:
                JTextField tipoEsporteQuadra = (JTextField) getComponentByName(atributosEspecificosPanel, "tipoEsporteQuadra");
                JCheckBox temCobertaQuadra = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temCobertaQuadra");
                JCheckBox temIluminacaoQuadra = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temIluminacaoQuadra");
                JCheckBox temVestiarioQuadra = (JCheckBox) getComponentByName(atributosEspecificosPanel, "temVestiarioQuadra");
                JTextField tipoPisoQuadra = (JTextField) getComponentByName(atributosEspecificosPanel, "tipoPisoQuadra");
                atributosEspecificos.append(tipoEsporteQuadra.getText()).append(";");
                atributosEspecificos.append(temCobertaQuadra.isSelected()).append(";");
                atributosEspecificos.append(temIluminacaoQuadra.isSelected()).append(";");
                atributosEspecificos.append(temVestiarioQuadra.isSelected()).append(";");
                atributosEspecificos.append(tipoPisoQuadra.getText());
                break;
            default:
                break;
        }

        String resposta = client.cadastrarEspaco(nome, localizacao, capacidade, descricao, tipo.name(), atributosEspecificos.toString());
        if (resposta.startsWith("SUCESSO")) {
            JOptionPane.showMessageDialog(this, "Espaço cadastrado com sucesso!");
            success = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar espaço: " + resposta, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Component getComponentByName(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
            if (comp instanceof Container) {
                Component found = getComponentByName((Container) comp, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void atualizarAtributosEspecificosPanel(TipoEspaco tipo) {
        atributosEspecificosPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        switch (tipo) {
            case SALA_AULA:
                JCheckBox temProjetorSalaAula = new JCheckBox("Tem Projetor");
                temProjetorSalaAula.setName("temProjetorSalaAula");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temProjetorSalaAula, gbc);

                JCheckBox temArCondicionadoSalaAula = new JCheckBox("Tem Ar Condicionado");
                temArCondicionadoSalaAula.setName("temArCondicionadoSalaAula");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temArCondicionadoSalaAula, gbc);

                JCheckBox temQuadroSalaAula = new JCheckBox("Tem Quadro");
                temQuadroSalaAula.setName("temQuadroSalaAula");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temQuadroSalaAula, gbc);

                JSpinner numeroComputadoresSalaAula = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
                numeroComputadoresSalaAula.setName("numeroComputadoresSalaAula");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(new JLabel("Número de Computadores:"), gbc);
                gbc.gridx = 1; atributosEspecificosPanel.add(numeroComputadoresSalaAula, gbc);
                break;
            case LABORATORIO:
                JSpinner numeroComputadoresLab = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
                numeroComputadoresLab.setName("numeroComputadoresLab");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(new JLabel("Número de Computadores:"), gbc);
                gbc.gridx = 1; atributosEspecificosPanel.add(numeroComputadoresLab, gbc);

                JTextField tipoLaboratorioField = new JTextField(20);
                tipoLaboratorioField.setName("tipoLaboratorioField");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(new JLabel("Tipo de Laboratório:"), gbc);
                gbc.gridx = 1; atributosEspecificosPanel.add(tipoLaboratorioField, gbc);

                JCheckBox temProjetorLab = new JCheckBox("Tem Projetor");
                temProjetorLab.setName("temProjetorLab");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temProjetorLab, gbc);

                JCheckBox temArCondicionadoLab = new JCheckBox("Tem Ar Condicionado");
                temArCondicionadoLab.setName("temArCondicionadoLab");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temArCondicionadoLab, gbc);

                JTextField equipamentosEspeciaisField = new JTextField(20);
                equipamentosEspeciaisField.setName("equipamentosEspeciaisField");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(new JLabel("Equipamentos Especiais:"), gbc);
                gbc.gridx = 1; atributosEspecificosPanel.add(equipamentosEspeciaisField, gbc);
                break;
            case AUDITORIO:
                JCheckBox temProjetorAud = new JCheckBox("Tem Projetor");
                temProjetorAud.setName("temProjetorAud");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temProjetorAud, gbc);

                JCheckBox temSomProfissionalAud = new JCheckBox("Tem Som Profissional");
                temSomProfissionalAud.setName("temSomProfissionalAud");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temSomProfissionalAud, gbc);

                JCheckBox temPalcoAud = new JCheckBox("Tem Palco");
                temPalcoAud.setName("temPalcoAud");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temPalcoAud, gbc);

                JCheckBox temArCondicionadoAud = new JCheckBox("Tem Ar Condicionado");
                temArCondicionadoAud.setName("temArCondicionadoAud");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temArCondicionadoAud, gbc);

                JSpinner numeroMicrofonesAud = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
                numeroMicrofonesAud.setName("numeroMicrofonesAud");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(new JLabel("Número de Microfones:"), gbc);
                gbc.gridx = 1; atributosEspecificosPanel.add(numeroMicrofonesAud, gbc);

                JTextField tipoAssentosAud = new JTextField(20);
                tipoAssentosAud.setName("tipoAssentosAud");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(new JLabel("Tipo de Assentos:"), gbc);
                gbc.gridx = 1; atributosEspecificosPanel.add(tipoAssentosAud, gbc);
                break;
            case SALA_REUNIAO:
                JCheckBox temProjetorReuniao = new JCheckBox("Tem Projetor");
                temProjetorReuniao.setName("temProjetorReuniao");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temProjetorReuniao, gbc);

                JCheckBox temArCondicionadoReuniao = new JCheckBox("Tem Ar Condicionado");
                temArCondicionadoReuniao.setName("temArCondicionadoReuniao");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temArCondicionadoReuniao, gbc);

                JCheckBox temTelefoneReuniao = new JCheckBox("Tem Telefone");
                temTelefoneReuniao.setName("temTelefoneReuniao");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temTelefoneReuniao, gbc);

                JCheckBox temVideoconferenciaReuniao = new JCheckBox("Tem Videoconferência");
                temVideoconferenciaReuniao.setName("temVideoconferenciaReuniao");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temVideoconferenciaReuniao, gbc);

                JTextField tipoMesaReuniao = new JTextField(20);
                tipoMesaReuniao.setName("tipoMesaReuniao");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(new JLabel("Tipo de Mesa:"), gbc);
                gbc.gridx = 1; atributosEspecificosPanel.add(tipoMesaReuniao, gbc);
                break;
            case QUADRA_ESPORTIVA:
                JTextField tipoEsporteQuadra = new JTextField(20);
                tipoEsporteQuadra.setName("tipoEsporteQuadra");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(new JLabel("Tipo de Esporte:"), gbc);
                gbc.gridx = 1; atributosEspecificosPanel.add(tipoEsporteQuadra, gbc);

                JCheckBox temCobertaQuadra = new JCheckBox("Tem Cobertura");
                temCobertaQuadra.setName("temCobertaQuadra");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temCobertaQuadra, gbc);

                JCheckBox temIluminacaoQuadra = new JCheckBox("Tem Iluminação");
                temIluminacaoQuadra.setName("temIluminacaoQuadra");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temIluminacaoQuadra, gbc);

                JCheckBox temVestiarioQuadra = new JCheckBox("Tem Vestiário");
                temVestiarioQuadra.setName("temVestiarioQuadra");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(temVestiarioQuadra, gbc);

                JTextField tipoPisoQuadra = new JTextField(20);
                tipoPisoQuadra.setName("tipoPisoQuadra");
                gbc.gridx = 0; gbc.gridy = y++; atributosEspecificosPanel.add(new JLabel("Tipo de Piso:"), gbc);
                gbc.gridx = 1; atributosEspecificosPanel.add(tipoPisoQuadra, gbc);
                break;
            default:
                break;
        }
        atributosEspecificosPanel.revalidate();
        atributosEspecificosPanel.repaint();
    }
}