package com.example.Chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByTimestampServerGreaterThanOrderByTimestampServerAsc(Instant timestamp);

    Optional<Mensagem> findByUniqueIdemKey(String idemKey);
}
