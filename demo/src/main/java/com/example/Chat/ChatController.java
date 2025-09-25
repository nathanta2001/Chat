package com.example.Chat;


import com.example.Chat.Mensagem;
import com.example.Chat.MessageRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/groups")
public class ChatController {

    private final MessageRepository messageRepository;

    public ChatController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // POST /groups/{id}/messages
    @PostMapping("/{id}/messages")
    public ResponseEntity<Mensagem> postMessage(@PathVariable String id, @RequestBody Mensagem mensagem) {
        Optional<Mensagem> temMensagem = messageRepository.findByUniqueIdemKey(mensagem.getIdemKey());
        if (temMensagem.isPresent()) {
            return ResponseEntity.ok(temMensagem.get());
        }

        mensagem.setTimestampServer(Instant.now());
        Mensagem mensagemSalva = messageRepository.save(mensagem);
        return ResponseEntity.ok(mensagemSalva);
    }

    // GET /groups/{id}/messages?since=123456
    @GetMapping("/{id}/messages")
    public List<Mensagem> getMensagens(@PathVariable String id, @RequestParam(required = false) Long since) {
        if (since != null) {
            return messageRepository.findByTimestampServerGreaterThanOrderByTimestampServerAsc(
                    Instant.ofEpochMilli(since)
            );
        }
        return messageRepository.findAll();
    }
}


