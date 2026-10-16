package fr.pmevent.repository;

import fr.pmevent.entity.GuestGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuestGroupRepository extends JpaRepository<GuestGroupEntity, UUID> {
    List<GuestGroupEntity> findByEventIdOrderByNameAsc(UUID eventId);

    boolean existsByEventIdAndNameIgnoreCase(UUID eventId, String name);

    boolean existsByEventIdAndNameIgnoreCaseAndIdNot(UUID eventId, String name, UUID id);
}
