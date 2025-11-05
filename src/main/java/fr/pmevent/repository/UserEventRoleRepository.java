package fr.pmevent.repository;

import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.entity.UserEventRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEventRoleRepository extends JpaRepository<UserEventRoleEntity, Long> {
    Optional<UserEventRoleEntity> findByUserAndEvent(UserEntity user, EventEntity event);

    List<UserEventRoleEntity> findByEvent(EventEntity event);

    @Modifying
    @Query("DELETE FROM UserEventRoleEntity uer WHERE uer.user.id = :userId")
    void deleteByUserId(Long userId);
}
