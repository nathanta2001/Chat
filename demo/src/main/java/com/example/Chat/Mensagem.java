package com.example.Chat;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mensagem") // Adicionando o nome da tabela para clareza
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(unique = true)
    private String idemKey;
    private String nome;
    private String txt;
    private Instant timestampServer;
    private Instant timestampClient;

    // Construtor vazio (necessário para JPA)
    public Mensagem() {
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getIdemKey() {
        return idemKey;
    }

    public String getNome() {
        return nome;
    }

    public String getTxt() {
        return txt;
    }

    public Instant getTimestampServer() {
        return timestampServer;
    }

    public Instant getTimestampClient() {
        return timestampClient;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setIdemKey(String idemKey) {
        this.idemKey = idemKey;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public void setTimestampServer(Instant timestampServer) {
        this.timestampServer = timestampServer;
    }

    public void setTimestampClient(Instant timestampClient) {
        this.timestampClient = timestampClient;
    }
}