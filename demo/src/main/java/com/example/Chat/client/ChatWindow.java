package com.example.Chat.client;

import com.example.Chat.common.Mensagem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class ChatWindow extends JFrame implements ActionListener, KeyListener {

    private ChatApiService chatApiService;
    private String nome;

    private long groupIdValue;
    private ScheduledExecutorService scheduler;
    private String groupId;
    private long lastTimestamp;
    private static final long serialVersionUID = 1L;
    private JTextArea txt;
    private JTextField textMsg;
    private JButton btnSend;
    private JButton btnSair;
    private JLabel lblHistorico;

    private JLabel lblMsg;
    private JPanel pnlContent;



    public static void main(String[] args) {
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);

        if (loginDialog.isSucceeded()) {
            ChatWindow chatWindow = new ChatWindow(
                    loginDialog.getUserName(),
                    loginDialog.getSelectedGroup(),
                    loginDialog.getSelectedGroupId(),
                    loginDialog.getServerUrl()
            );
            chatWindow.setVisible(true);
            chatWindow.iniciarPollingMensagens();
        } else {
            System.exit(0);
        }

    }
    public ChatWindow(String userName, String groupName, long groupId, String serverUrl) {
        this.nome = userName;
        this.groupId = groupName;
        this.groupIdValue = groupId;
        pnlContent = new JPanel();
        txt = new JTextArea(10, 20);
        txt.setEditable(false);
        txt.setBackground(new Color(240, 240, 240));
        textMsg = new JTextField(20);
        lblHistorico = new JLabel("Histórico");
        lblMsg = new JLabel("Mensagem");
        btnSend = new JButton("Enviar");
        btnSend.setToolTipText("Enviar Mensagem");
        btnSair = new JButton("Sair");
        btnSair.setToolTipText("Sair do Chat");
        btnSend.addActionListener(this);
        btnSair.addActionListener(this);
        btnSend.addKeyListener(this);
        textMsg.addKeyListener(this);
        JScrollPane scroll = new JScrollPane(txt);
        txt.setLineWrap(true);
        pnlContent.add(lblHistorico);
        pnlContent.add(scroll);
        pnlContent.add(lblMsg);
        pnlContent.add(textMsg);
        pnlContent.add(btnSair);
        pnlContent.add(btnSend);
        pnlContent.setBackground(Color.LIGHT_GRAY);
        txt.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
        textMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));

        setTitle(this.nome + " - Sala: " + this.groupId);
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(250, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.chatApiService = new ChatApiService(serverUrl, this.txt);
        txt.append("Conectado ao servidor na sala '" + this.groupId + "'\r\n");

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Sair();
            }
        });
    }


    public void iniciarPollingMensagens() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {

                List<Mensagem> novasMensagens = chatApiService.getMessage(this.groupId, lastTimestamp, 100);

                for (Mensagem msg : novasMensagens) {
                    txt.append(msg.getNome() + " disse: " + msg.getTxt() + "\r\n");
                    this.lastTimestamp = msg.getTimestampServer().toEpochMilli();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void EnviarMsg(String msg) throws IOException{
        if(msg.trim().isEmpty()){
            return;
        }

        String idemKey = UUID.randomUUID().toString();
        chatApiService.tentarNovamente(this.groupId, this.nome, msg, idemKey);
        textMsg.setText("");
    }


    public void Sair() {
        chatApiService.logout(this.groupIdValue, this.nome);

        if (scheduler != null) {
            scheduler.shutdown();
        }
        this.dispose();
        System.exit(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btnSend){
            try {
                EnviarMsg(textMsg.getText());
            } catch (IOException e1){
                e1.printStackTrace();
            }
        } else if (e.getSource() == btnSair) {
            Sair();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                EnviarMsg(textMsg.getText());
            }catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
