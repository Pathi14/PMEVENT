package fr.pmevent.repository;

import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, Long> {
    List<GuestEntity> findByEventId(Long eventId);

    void deleteAllByEvent(EventEntity event);
}
