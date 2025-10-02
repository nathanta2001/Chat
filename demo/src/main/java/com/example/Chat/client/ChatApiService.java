package com.example.Chat.client;

import com.example.Chat.common.Grupo;
import com.example.Chat.common.Mensagem;
import com.example.Chat.common.Nomes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.swing.*;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChatApiService {

    private final HttpClient httpClient;
    private final String serverUrl;
    private final ObjectMapper objectMapper;
    private JTextArea textArea;


    public ChatApiService(String serverUrl, JTextArea textArea) {
        this.httpClient = HttpClient.newHttpClient();
        this.serverUrl = serverUrl;
        this.objectMapper = new ObjectMapper();
        this.textArea = textArea;

        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<Mensagem> getMessage(String groupId, Long sinceTimestamp, int limit) throws Exception {
        try {
            String url = serverUrl + "/groups/" + groupId + "/messages?limit=" + limit;
            if (sinceTimestamp != null && sinceTimestamp > 0) {
                url += "&since=" + sinceTimestamp;
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
            throw e;
        }
    }

    public void tentarNovamente(String groupId, String nome, String txt, String idemKey) {
        int maxTentativas = 5;
        long baseDelayMs = 1000;
        Random random = new Random();

        for (int i = 0; i < maxTentativas; i++) {
            try {
                HttpResponse<String> response = sendPostRequest(groupId, nome, txt, idemKey);

                if(response.statusCode() == 200){
                    System.out.println("Mensagem enviada com sucesso na tentativa: "+ (i+1));
                    return;
                } else if (response.statusCode() >= 500) {
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
        msg.setTimestampClient(Instant.ofEpochMilli(System.currentTimeMillis()));

        String jsonPayLoad = objectMapper.writeValueAsString(msg);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/groups/" + groupId + "/messages"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayLoad))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public List<Grupo> getGroups() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/groups"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Grupo>>() {});
        } else {
            System.err.println("Erro ao buscar grupos: " + response.body());
            return Collections.emptyList();
        }
    }

    public Grupo createGroup(String groupName) throws Exception {
        Grupo newGroup = new Grupo();
        newGroup.setName(groupName);
        String jsonPayload = objectMapper.writeValueAsString(newGroup);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/groups"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) { // 201 = Created
            return objectMapper.readValue(response.body(), Grupo.class);
        } else {
            System.err.println("Erro ao criar grupo: " + response.body());
            return null;
        }
    }

    public boolean tryConnect() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/groups/connect"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    public void disconnect() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/groups/disconnect"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public int registrarNomes(long groupId, String nome) throws Exception {
        Nomes participant = new Nomes();
        participant.setName(nome);
        String jsonPayload = objectMapper.writeValueAsString(participant);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/groups/" + groupId + "/nomes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    public void unregisterNames(long groupId, String nomes) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/groups/" + groupId + "/nomes/" + nomes))
                    .DELETE()
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.err.println("Erro ao tentar fazer logout (pode ser ignorado): " + e.getMessage());
        }
    }

}


