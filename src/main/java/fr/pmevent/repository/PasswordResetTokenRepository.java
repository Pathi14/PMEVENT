package fr.pmevent.repository;

import fr.pmevent.entity.PasswordResetTokenEntity;
import fr.pmevent.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    void deleteByUser(UserEntity user);
}