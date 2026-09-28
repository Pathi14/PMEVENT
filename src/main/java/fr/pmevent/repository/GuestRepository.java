package fr.pmevent.repository;

import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, UUID> {
    List<GuestEntity> findByEventId(UUID eventId);

    @Query("SELECT COALESCE(SUM(guest.number_places), 0) FROM GuestEntity guest WHERE guest.event.id = :eventId")
    long sumNumberPlacesByEventId(@Param("eventId") UUID eventId);

    void deleteAllByEvent(EventEntity event);

    Optional<GuestEntity> findByQrCodeToken(String qrCodeToken);

}
