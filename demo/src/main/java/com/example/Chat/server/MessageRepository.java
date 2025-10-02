package com.example.Chat.server;

import com.example.Chat.common.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface MessageRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByGroupIdAndTimestampServerGreaterThanOrderByTimestampServerAsc(String groupId, Instant timestamp, Pageable pageable);

    List<Mensagem> findByGroupIdOrderByTimestampServerAsc(String groupId, Pageable pageable);
    Optional<Mensagem> findByIdemKey(String idemKey);

}
