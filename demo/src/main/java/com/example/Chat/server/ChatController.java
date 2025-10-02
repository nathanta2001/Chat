package com.example.Chat.server;


import com.example.Chat.common.Grupo;
import com.example.Chat.common.Mensagem;
import com.example.Chat.common.Nomes;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.concurrent.Semaphore;

@RestController
@RequestMapping("/groups")
public class ChatController {

    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final Semaphore connectionSemaphore = new Semaphore(5);

    private final ActiveClientService activeClientService;

    private final NamesRepository namesRepository;

    public ChatController(MessageRepository messageRepository, GroupRepository groupRepository, ActiveClientService activeClientService, NamesRepository namesRepository) {
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.activeClientService = activeClientService;
        this.namesRepository = namesRepository;
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Mensagem> postMessage(@PathVariable String id, @RequestBody Mensagem mensagem) {
        try {
            connectionSemaphore.acquire();
            Optional<Mensagem> temMensagem = messageRepository.findByIdemKey(mensagem.getIdemKey());
            if (temMensagem.isPresent()) {
                return ResponseEntity.ok(temMensagem.get());
            }
            mensagem.setTimestampServer(Instant.now().truncatedTo(ChronoUnit.MILLIS));
            mensagem.setGroupId(id);
            Mensagem mensagemSalva = messageRepository.save(mensagem);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            connectionSemaphore.release();
        }
        return null;
    }

    @GetMapping("/{id}/messages")
    public List<Mensagem> getMensagens(@PathVariable String id,
                                       @RequestParam(required = false) Long since,
                                       @RequestParam(defaultValue = "50") int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        try {
            connectionSemaphore.acquire();
            if (since != null && since > 0) {
                return messageRepository.findByGroupIdAndTimestampServerGreaterThanOrderByTimestampServerAsc(
                        id, Instant.ofEpochMilli(since), pageable);
            } else {
                return messageRepository.findByGroupIdOrderByTimestampServerAsc(id, pageable);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } finally {
            connectionSemaphore.release();
        }
    }

    @GetMapping
    public List<Grupo> getGroups() {
        return groupRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Grupo> createGroup(@RequestBody Grupo newGroup) {
        Optional<Grupo> existingGroup = groupRepository.findByName(newGroup.getName());
        if (existingGroup.isPresent()) {
            return ResponseEntity.ok(existingGroup.get());
        }
        Grupo savedGroup = groupRepository.save(newGroup);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGroup);
    }

    @DeleteMapping("/groups/{id}")
    @Transactional
    public ResponseEntity<Void> deleteGroup(@PathVariable long id) {
        String groupName = groupRepository.findById(id).map(Grupo::getName).orElse(null);

        if (groupName == null) {
            return ResponseEntity.notFound().build();
        }

        groupRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/connect")
    public ResponseEntity<String> connect() {
        if (activeClientService.tryConnect()) {
            return ResponseEntity.ok("Conectado com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Servidor lotado. Tente mais tarde.");
        }
    }

    @PostMapping("/disconnect")
    public void disconnect() {
        activeClientService.disconnect();
    }

    @PostMapping("/{groupId}/nomes")
    public ResponseEntity<Nomes> registrarNomes(@PathVariable Long groupId, @RequestBody Nomes namesRequest) {

        Optional<Grupo> grupoOpt = groupRepository.findById(groupId);
        if (grupoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Nomes> existingName = namesRepository.findByGrupoIdAndName(groupId, namesRequest.getName());
        if (existingName.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        namesRequest.setGrupo(grupoOpt.get());
        Nomes savedName = namesRepository.save(namesRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedName);
    }

    @DeleteMapping("/{groupId}/nomes/{name}")
    public ResponseEntity<Void> unregisterNames(@PathVariable Long groupId, @PathVariable String name) {
        namesRepository.deleteByGrupoIdAndName(groupId, name);
        return ResponseEntity.noContent().build();
    }

}


