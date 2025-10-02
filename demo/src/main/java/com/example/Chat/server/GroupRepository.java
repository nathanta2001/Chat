package com.example.Chat.server;

import com.example.Chat.common.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Grupo, Long> {
    Optional<Grupo> findByName(String name);
}