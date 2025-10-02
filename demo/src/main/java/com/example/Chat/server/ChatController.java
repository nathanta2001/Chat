package com.example.Chat.server;


import com.example.Chat.common.Grupo;
import com.example.Chat.common.Mensagem;
import com.example.Chat.common.Nomes;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<Mensagem> postMessage(@PathVariable String id, @RequestBody Mensagem mensagem, HttpServletRequest request) {
        try {
            updateUserActivity(request);
            connectionSemaphore.acquire();

            Optional<Mensagem> temMensagem = messageRepository.findByIdemKey(mensagem.getIdemKey());
            if (temMensagem.isPresent()) {
                return ResponseEntity.ok(temMensagem.get());
            }

            mensagem.setTimestampServer(Instant.now().truncatedTo(ChronoUnit.MILLIS));
            mensagem.setGroupId(id);
            Mensagem mensagemSalva = messageRepository.save(mensagem);

            return ResponseEntity.status(HttpStatus.CREATED).body(mensagemSalva);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            connectionSemaphore.release();
        }
    }
    @GetMapping("/{id}/messages")
    public List<Mensagem> getMensagens(@PathVariable String id,
                                       @RequestParam(required = false) Long since,
                                       @RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(0, limit);

        try {
            updateUserActivity(request);
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
    public ResponseEntity<String> connect(@RequestBody Nomes participant) {
        if (participant == null || participant.getGrupo() == null || participant.getName() == null) {
            return ResponseEntity.badRequest().body("Informações do participante e do grupo são necessárias.");
        }
        String clientId = ActiveClientService.getClientId(participant.getGrupo().getId(), participant.getName());
        String removedClientId = activeClientService.tryConnectAndMakeSpace(clientId);

        if ("SERVER_FULL".equals(removedClientId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Servidor cheio e todos os clientes estão ativos.");
        }

        if (removedClientId != null) {
            String[] parts = removedClientId.split(":");
            long groupId = Long.parseLong(parts[0]);
            String name = parts[1];
            namesRepository.deleteByGrupoIdAndName(groupId, name);
        }
        return ResponseEntity.ok("Conectado com sucesso.");
    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<Void> logout(@RequestBody Nomes participant) {
        if (participant == null || participant.getGrupo() == null || participant.getName() == null) {
            return ResponseEntity.badRequest().build();
        }

        long groupId = participant.getGrupo().getId();
        String name = participant.getName();

        String clientId = ActiveClientService.getClientId(groupId, name);
        activeClientService.disconnect(clientId);

        namesRepository.deleteByGrupoIdAndName(groupId, name);

        return ResponseEntity.noContent().build();
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


    private void updateUserActivity(HttpServletRequest request) {
        String clientId = request.getHeader("X-Client-ID");
        if (clientId != null && !clientId.isEmpty()) {
            activeClientService.updateActivity(clientId);
        }
    }

}


