package com.example.Chat.client;

import com.example.Chat.common.Grupo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginDialog extends JDialog {

    private final JTextField ipField = new JTextField("127.0.0.1", 15);
    private final JTextField portField = new JTextField("8081", 15);
    private final JTextField nameField = new JTextField(15);
    private final JComboBox<Object> groupComboBox = new JComboBox<>();

    // Botões
    private final JButton refreshButton = new JButton("Atualizar Salas");
    private final JButton createGroupButton = new JButton("Criar Sala");
    private final JButton connectButton = new JButton("Conectar");

    private String serverUrl;
    private String userName;
    private String selectedGroup;
    private ChatApiService chatApiService;
    private long selectedGroupId = -1;
    private List<Grupo> availableGroups;
    private boolean succeeded = false;

    public LoginDialog(Frame parent) {
        super(parent, "Conectar ao Chat", true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Linha 1: IP
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("IP do Servidor:"), gbc);
        gbc.gridx = 1;
        panel.add(ipField, gbc);

        // Linha 2: Porta
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Porta:"), gbc);
        gbc.gridx = 1;
        panel.add(portField, gbc);

        // Linha 3: Nome
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Seu Nome:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Linha 4: Salas
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Sala:"), gbc);
        gbc.gridx = 1;
        panel.add(groupComboBox, gbc);
        groupComboBox.setEnabled(false);

        JPanel groupButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        groupButtonsPanel.add(refreshButton);
        groupButtonsPanel.add(createGroupButton);
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(groupButtonsPanel, gbc);
        createGroupButton.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(connectButton);
        connectButton.setEnabled(false);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.PAGE_END);

        refreshButton.addActionListener(e -> onRefresh());
        createGroupButton.addActionListener(e -> onCreateGroup());
        connectButton.addActionListener(e -> onConnect());

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void onRefresh() {
        try {
            this.serverUrl = "http://" + ipField.getText() + ":" + portField.getText();
            this.chatApiService = new ChatApiService(serverUrl, null);
            this.availableGroups = chatApiService.getGroups();

            // Limpa e preenche o ComboBox
            groupComboBox.removeAllItems();
            groupComboBox.addItem("Selecione uma sala...");
            for (Grupo group : this.availableGroups) {
                groupComboBox.addItem(group.getName());
            }
            groupComboBox.setEnabled(true);
            createGroupButton.setEnabled(true);
            connectButton.setEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível buscar as salas.\nVerifique o IP/Porta e se o servidor está online.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCreateGroup() {
        String newGroupName = JOptionPane.showInputDialog(this, "Digite o nome da nova sala:");
        if (newGroupName != null && !newGroupName.trim().isEmpty()) {
            try {
                chatApiService.createGroup(newGroupName);
                onRefresh();
                groupComboBox.setSelectedItem(newGroupName);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível criar a sala.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onConnect() {
        try {
            List<Grupo> groups = chatApiService.getGroups();
            String selectedGroupName = groupComboBox.getSelectedItem().toString();
            long selectedGroupId = -1;
            for (Grupo group : groups) {
                if (group.getName().equals(selectedGroupName)) {
                    selectedGroupId = group.getId();
                    break;
                }
            }

            if (selectedGroupId == -1) {
                JOptionPane.showMessageDialog(this, "Não foi possível encontrar o ID da sala selecionada.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int registrationStatus = chatApiService.registrarNomes(selectedGroupId, nameField.getText().trim());

            if (registrationStatus == 201) {
                this.userName = nameField.getText().trim();
                this.selectedGroup = groupComboBox.getSelectedItem().toString();
                for (Grupo group : this.availableGroups) {
                    if (group.getName().equals(this.selectedGroup)) {
                        this.selectedGroupId = group.getId();
                        break;
                    }
                }
                this.succeeded = true;
                dispose();
            } else if (registrationStatus == 409) {
                JOptionPane.showMessageDialog(this, "Este nome já está em uso nesta sala. Por favor, escolha outro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Ocorreu um erro desconhecido ao registar o nome (Código: " + registrationStatus + ")", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível conectar para registar o nome.", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }
    public long getSelectedGroupId() {
        return selectedGroupId;
    }
    public String getServerUrl() { return serverUrl; }
    public String getUserName() { return userName; }
    public String getSelectedGroup() { return selectedGroup; }
}