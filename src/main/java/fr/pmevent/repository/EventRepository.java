package fr.pmevent.repository;

import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.enums.EventRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    boolean existsByName(String name);

    List<EventEntity> findByPublicEventTrue();

    @Query("""
                SELECT uer.event
                FROM UserEventRoleEntity uer
                WHERE uer.user = :user
                AND uer.role IN :roles
            """)
    List<EventEntity> findEventsByUserAndRole(@Param("user") UserEntity user, @Param("roles") List<EventRole> roles);

}
