package com.example.Chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChatClient {

    private final HttpClient httpClient;
    private final String serverUrl;
    private final ObjectMapper objectMapper;
    private JTextArea textArea;



    public ChatClient(String serverUrl, JTextArea textArea) {
        this.httpClient = HttpClient.newHttpClient();
        this.serverUrl = serverUrl;
        this.objectMapper = new ObjectMapper();
        this.textArea = textArea;

        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<Mensagem> getMessage(String groupId, Long sinceTimestamp) throws Exception { // Adicione 'throws Exception' ou trate o erro internamente
        try {
            // Em ChatClient.java
            String url = serverUrl + "/groups/" + groupId + "/messages";

            if(sinceTimestamp != null){
                url += "?since=" + sinceTimestamp;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<Mensagem>>() {});
            } else {
                System.out.println("Erro ao buscar mensagens " + response.body());
                return Collections.emptyList(); // Retorne null ou uma lista vazia em caso de erro
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Lança a exceção para que a classe que chama possa tratá-la
        }
    }

    public void tentarNovamente(String groupId, String nome, String txt, String idemKey) {
        int maxTentativas = 5;
        long baseDelayMs = 1000;
        Random random = new Random();

        for (int i = 0; i < maxTentativas; i++) {
            try {
                // Use a instância de httpClient da classe
                HttpResponse<String> response = sendPostRequest(groupId, nome, txt, idemKey);

                if(response.statusCode() == 200){
                    System.out.println("Mensagem enviada com sucesso na tentativa: "+ (i+1));
                    // Código para adicionar a mensagem na sua própria janela
                    textArea.append(nome + " disse: " + txt + "\r\n");
                    return;
                } else if (response.statusCode() >= 500) { // Erro do servidor
                    System.out.println("Erro do servidor, tentando novamente... Tentativa: " + (i + 1));
                    long delay = (long) (baseDelayMs * Math.pow(2, i) + random.nextInt(100));
                    Thread.sleep(delay);
                } else { // Erro do cliente, não tenta novamente
                    System.out.println("Erro do cliente, não há retentativa. Código: " + response.statusCode());
                    return;
                }
            } catch (Exception e) {
                // Em caso de exceção de rede, loga o erro e continua para a próxima tentativa
                e.printStackTrace();
            }
        }
        System.out.println("Falha ao enviar a mensagem após " + maxTentativas + " tentativas.");
    }

    private HttpResponse<String> sendPostRequest(String groupId, String nome, String txt, String idemKey) throws Exception {
        Mensagem msg = new Mensagem();
        msg.setNome(nome);
        msg.setTxt(txt);
        msg.setIdemKey(idemKey);
        // Instant.ofEpochSecond() espera segundos, mas System.currentTimeMillis() retorna milissegundos
        msg.setTimestampClient(Instant.ofEpochMilli(System.currentTimeMillis()));

        String jsonPayLoad = objectMapper.writeValueAsString(msg);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/groups/" + groupId + "/messages"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayLoad))
                .build();

        // Use a instância de httpClient da classe
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
