package fr.pmevent.service;

import fr.pmevent.common.dto.guest.GuestGroupRequest;
import fr.pmevent.common.dto.guest.GuestGroupResponse;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import fr.pmevent.entity.GuestGroupEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.mapper.GuestMapper;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.GuestGroupRepository;
import fr.pmevent.repository.GuestRepository;
import fr.pmevent.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GuestGroupService {

    private final GuestGroupRepository guestGroupRepository;
    private final GuestRepository guestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventPermissionService eventPermissionService;
    private final GuestMapper guestMapper;
    private final GuestService guestService;

    @Transactional(readOnly = true)
    public List<GuestGroupResponse> getGroupsForEvent(UUID eventId) {
        EventEntity event = getEvent(eventId);
        requireGuestManagement(event);
        return guestGroupRepository.findByEventIdOrderByNameAsc(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GuestGroupResponse createGroup(UUID eventId, GuestGroupRequest request) {
        EventEntity event = getEvent(eventId);
        requireGuestManagement(event);
        validateRequest(request);
        String name = request.name().trim();
        if (guestGroupRepository.existsByEventIdAndNameIgnoreCase(eventId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un groupe porte déjà ce nom pour cet événement.");
        }

        GuestGroupEntity group = new GuestGroupEntity();
        group.setEvent(event);
        group.setName(name);
        group.setMaxPeople(request.maxPeople());
        return toResponse(guestGroupRepository.save(group));
    }

    @Transactional
    public GuestGroupResponse updateGroup(UUID groupId, GuestGroupRequest request) {
        GuestGroupEntity group = getGroup(groupId);
        requireGuestManagement(group.getEvent());
        validateRequest(request);

        String name = request.name().trim();
        if (guestGroupRepository.existsByEventIdAndNameIgnoreCaseAndIdNot(
                group.getEvent().getId(), name, groupId
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un groupe porte déjà ce nom pour cet événement.");
        }
        long occupiedPlaces = guestRepository.sumNumberPlacesByGuestGroupId(groupId);
        if (request.maxPeople() < occupiedPlaces) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La capacité maximale ne peut pas être inférieure au nombre de places déjà attribuées."
            );
        }

        group.setName(name);
        group.setMaxPeople(request.maxPeople());
        return toResponse(guestGroupRepository.save(group));
    }

    @Transactional
    public void deleteGroup(UUID groupId, boolean deleteGuests) {
        GuestGroupEntity group = getGroup(groupId);
        requireGuestManagement(group.getEvent());
        List<GuestEntity> guests = guestRepository.findByGuestGroupIdOrderByNameAscFirstnameAsc(groupId);
        if (deleteGuests) {
            guests.forEach(guest -> guestService.removeGuest(guest.getId()));
        } else {
            guests.forEach(guest -> guest.setGuestGroup(null));
            guestRepository.saveAll(guests);
        }
        guestGroupRepository.delete(group);
    }

    @Transactional
    public GuestGroupResponse addGuest(UUID groupId, UUID guestId) {
        GuestGroupEntity group = getGroup(groupId);
        requireGuestManagement(group.getEvent());
        GuestEntity guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invité introuvable."));
        requireGuestManagement(guest.getEvent());
        if (!guest.getEvent().getId().equals(group.getEvent().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'invité et le groupe doivent appartenir au même événement.");
        }
        if (guest.getGuestGroup() != null && !guest.getGuestGroup().getId().equals(groupId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet invité appartient déjà à un autre groupe.");
        }
        if (guest.getGuestGroup() == null) {
            long occupiedPlaces = guestRepository.sumNumberPlacesByGuestGroupId(groupId);
            if (occupiedPlaces + guest.getNumber_places() > group.getMaxPeople()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La capacité maximale du groupe serait dépassée.");
            }
            guest.setGuestGroup(group);
            guestRepository.save(guest);
        }
        return toResponse(group);
    }

    @Transactional
    public GuestGroupResponse removeGuest(UUID groupId, UUID guestId) {
        GuestGroupEntity group = getGroup(groupId);
        requireGuestManagement(group.getEvent());
        GuestEntity guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invité introuvable."));
        requireGuestManagement(guest.getEvent());
        if (guest.getGuestGroup() == null || !guest.getGuestGroup().getId().equals(groupId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cet invité n'appartient pas à ce groupe.");
        }
        guest.setGuestGroup(null);
        guestRepository.save(guest);
        return toResponse(group);
    }

    private GuestGroupResponse toResponse(GuestGroupEntity group) {
        List<GuestEntity> guests = guestRepository.findByGuestGroupIdOrderByNameAscFirstnameAsc(group.getId());
        return new GuestGroupResponse(
                group.getId(),
                group.getName(),
                group.getMaxPeople(),
                guests.stream().mapToLong(guest -> guest.getNumber_places()).sum(),
                guests.stream().map(guestMapper::toResponse).toList()
        );
    }

    private EventEntity getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable."));
    }

    private GuestGroupEntity getGroup(UUID groupId) {
        return guestGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Groupe introuvable."));
    }

    private void validateRequest(GuestGroupRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom du groupe est obligatoire.");
        }
        if (request.maxPeople() == null || request.maxPeople() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La capacité maximale doit être supérieure à zéro.");
        }
    }

    private void requireGuestManagement(EventEntity event) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable."));
        eventPermissionService.requirePermission(
                event,
                user,
                EventPermissionService.Permission.MANAGE_GUESTS
        );
    }
}
