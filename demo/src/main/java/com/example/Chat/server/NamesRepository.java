package com.example.Chat.server;

import com.example.Chat.common.Nomes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface NamesRepository extends JpaRepository<Nomes, Long> {
    Optional<Nomes> findByGrupoIdAndName(Long groupId, String name);
    @Transactional
    void deleteByGrupoIdAndName(Long groupId, String name);

}