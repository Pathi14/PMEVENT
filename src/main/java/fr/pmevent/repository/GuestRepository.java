package fr.pmevent.repository;

import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, Long> {
    List<GuestEntity> findByEventId(Long eventId);

    void deleteAllByEvent(EventEntity event);

    Optional<GuestEntity> findByQrCodeToken(String qrCodeToken);

}
