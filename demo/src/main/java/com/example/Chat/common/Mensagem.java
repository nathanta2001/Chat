package com.example.Chat.common;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mensagem")
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

    private String groupId;


    public Mensagem() {
    }

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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
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