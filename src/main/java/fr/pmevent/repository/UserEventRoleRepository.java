package fr.pmevent.repository;

import fr.pmevent.entity.UserEventRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventRoleRepository extends JpaRepository<UserEventRoleEntity, Long> {
}
