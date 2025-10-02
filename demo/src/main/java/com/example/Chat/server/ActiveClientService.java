package com.example.Chat.server;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ActiveClientService {

    private final AtomicInteger activeClients = new AtomicInteger(0);
    private static final int MAX_CLIENTS = 5;

    // Construtor para logging
    public ActiveClientService() {
        // Este log só deve aparecer UMA VEZ quando o servidor inicia.
        // Se aparecer mais vezes, descobrimos a causa do problema.
        System.out.println(">>> ActiveClientService INICIADO. Contador em: " + activeClients.get());
    }

    public boolean tryConnect() {
        int currentCount = activeClients.incrementAndGet();

        if (currentCount > MAX_CLIENTS) {
            activeClients.decrementAndGet();
            return false;
        }

        System.out.println(">>> CONEXÃO ACEITA. Clientes ativos: " + currentCount);
        return true;
    }

    public void disconnect() {
        int currentCount = activeClients.decrementAndGet();
        System.out.println("--- Cliente desconectado ---");
        System.out.println("Clientes restantes: " + currentCount);
    }
}