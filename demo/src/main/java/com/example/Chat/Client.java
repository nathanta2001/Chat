package com.example.Chat;

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

public class Client extends JFrame implements ActionListener, KeyListener {

    private ChatClient chatClient; // Adicione esta linha
    private String nome;

    private ScheduledExecutorService scheduler;
    private String groupId;
    private long lastTimestamp;
    private static final long serialVersionUID = 1L;
    private JTextArea txt;
    private JTextField textMsg;
    private JButton btnSend;
    private JButton btnSair;
    private JLabel lblHistorico;
    private JLabel labelMensagem;
    private JLabel lblMsg;
    private JPanel pnlContent;
    private OutputStream ou ;
    private Writer ouw;
    private BufferedWriter bfw;
    private JTextField textIP;
    private JTextField textPorta;
    private JTextField txtNome;


    public static void main(String[] args) throws IOException{
        Client app = new Client();
        app.Conect();
    }
    public Client() throws IOException {
        JLabel labelMensagem = new JLabel("Verificar");
        textIP = new JTextField("127.0.0.1");
        textPorta = new JTextField("8080");
        txtNome = new JTextField("Cliente");
        Object[] texto = {lblMsg, textIP, textPorta, txtNome};
        JOptionPane.showMessageDialog(null, texto);
        pnlContent = new JPanel();
        txt = new JTextArea(10,20);
        txt.setEditable(false);
        txt.setBackground(new Color(240,240,240));
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
        txt.setBorder(BorderFactory.createEtchedBorder(Color.BLUE,Color.BLUE));
        textMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
        setTitle(txtNome.getText());
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(250,300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void Conect() throws IOException {
        String serverIp = textIP.getText();
        int serverPort = Integer.parseInt(textPorta.getText());
        String serverUrl = "http://" + serverIp + ":" + serverPort;

        textIP = new JTextField("127.0.0.1");
        // Altere a porta para 8080, que é a padrão do Spring Boot
        textPorta = new JTextField("8080");

        // Passe a referência da JTextArea no construtor do ChatClient
        this.chatClient = new ChatClient(serverUrl, this.txt);
        this.nome = txtNome.getText();
        this.groupId = "Grupo_1";

        txt.append("Conectado ao servidor \r\n"); // Use \r\n para nova linha
        iniciarPollingMensagens();
    }

    private void iniciarPollingMensagens() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Chama o método para buscar as mensagens
                // O seu método no ChatClient.java deve retornar uma lista de mensagens
                List<Mensagem> novasMensagens = chatClient.getMessage(this.groupId, lastTimestamp);

                // Adiciona as novas mensagens no JTextArea
                for (Mensagem msg : novasMensagens) {
                    txt.append(msg.getNome() + " disse: " + msg.getTxt() + "\r\n");
                    // A cada nova mensagem, atualize o timestamp para a próxima busca
                    this.lastTimestamp = msg.getTimestampServer().toEpochMilli();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS); // Polling a cada 1 segundo
    }

    public void EnviarMsg(String msg) throws IOException{
        if(msg.trim().isEmpty()){
            return;
        }

        String idemKey = UUID.randomUUID().toString();
        chatClient.tentarNovamente(this.groupId, this.nome, msg, idemKey);
        textMsg.setText("");
    }

    public void Sair() {
        // Parar o polling
        if (scheduler != null) {
            scheduler.shutdown();
        }
        this.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(btnSend.getActionCommand())){
            try {
                EnviarMsg(textMsg.getText());
            }catch (IOException e1){
                e1.printStackTrace();
            }
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
