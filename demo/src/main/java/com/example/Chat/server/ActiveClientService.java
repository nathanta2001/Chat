package com.example.Chat.server;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveClientService {

    private static final int MAX_CLIENTS = 5;
    private static final long INACTIVITY_TIMEOUT_MS = 10000;


    private final Map<String, Long> activeClients = new ConcurrentHashMap<>();


    public String tryConnectAndMakeSpace(String clientId) {
        if (activeClients.size() < MAX_CLIENTS) {
            activeClients.put(clientId, System.currentTimeMillis());
            System.out.println(">>> Vaga disponível. Cliente " + clientId + " conectado. Total: " + activeClients.size());
            return null;
        }

        for (Map.Entry<String, Long> entry : activeClients.entrySet()) {
            if (System.currentTimeMillis() - entry.getValue() > INACTIVITY_TIMEOUT_MS) {
                String inactiveClientId = entry.getKey();
                activeClients.remove(inactiveClientId);
                activeClients.put(clientId, System.currentTimeMillis());
                System.out.println("!!! Servidor cheio. Removido cliente inativo " + inactiveClientId + " para dar lugar a " + clientId);
                return inactiveClientId;
            }
        }

        System.out.println("!!! Servidor cheio. Conexão para " + clientId + " recusada.");
        return "SERVER_FULL";
    }

    public void updateActivity(String clientId) {
        if (activeClients.containsKey(clientId)) {
            activeClients.put(clientId, System.currentTimeMillis());
        }
    }

    public void disconnect(String clientId) {
        activeClients.remove(clientId);
        System.out.println("--- Cliente " + clientId + " desconectado. Total: " + activeClients.size());
    }

    public static String getClientId(long groupId, String name) {
        return groupId + ":" + name;
    }
}