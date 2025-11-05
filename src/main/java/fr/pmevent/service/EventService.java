package fr.pmevent.service;

import fr.pmevent.dto.event.CreateEventDto;
import fr.pmevent.dto.event.EventResponse;
import fr.pmevent.dto.event.UpdateEventDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.entity.UserEventRoleEntity;
import fr.pmevent.enums.EventRole;
import fr.pmevent.exception.AlreadyExistsException;
import fr.pmevent.mapper.EventMapper;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.UserEventRoleRepository;
import fr.pmevent.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final UserEventRoleRepository userEventRoleRepository;

    public void createEvent(CreateEventDto eventDto) {
        Optional<EventEntity> existingEvent = findByName(eventDto.getName());
        if (existingEvent.isPresent()) {
            throw new AlreadyExistsException("Event already exists");
        }

        EventEntity event = eventMapper.toEntity(eventDto);
        eventRepository.save(event);

        UserEntity user = getCurrentUser();

        UserEventRoleEntity userEventRole = new UserEventRoleEntity();
        userEventRole.setUser(user);
        userEventRole.setEvent(event);
        userEventRole.setRole(EventRole.CREATOR);

        userEventRoleRepository.save(userEventRole);
    }

    public void updateEvent(long id, UpdateEventDto updateEvent) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("This event doesn't exists"));

        UserEntity user = getCurrentUser();
        checkPermission(event, user, EventRole.CREATOR, EventRole.EDITOR);

        eventMapper.updateEventFromDto(updateEvent, event);
        eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(long id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("This event doesn't exists"));

        UserEntity user = getCurrentUser();
        checkPermission(event, user, EventRole.CREATOR);

        eventRepository.delete(event);
    }


    public EventResponse findEventById(Long id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("This event doesn't exists"));

        if (!event.isPublicEvent()) {
            UserEntity user = getCurrentUser();
            checkPermission(event, user, EventRole.CREATOR, EventRole.EDITOR, EventRole.VIEWER);
        }

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getLocation(),
                event.getStart_date(),
                event.getEnd_date(),
                event.getDescription(),
                event.isPublicEvent(),
                event.getCreate_date(),
                event.getUpdate_date()
        );
    }

    public List<EventResponse> getAllPublicEvents() {
        List<EventEntity> publicEvents = eventRepository.findByPublicEventTrue();
        return publicEvents.stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    public List<EventResponse> getAllViewerEvents() {
        UserEntity user = getCurrentUser();
        List<EventRole> roles = List.of(EventRole.VIEWER, EventRole.CREATOR, EventRole.EDITOR);
        return eventRepository.findEventsByUserAndRole(user, roles)
                .stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getName(),
                        event.getLocation(),
                        event.getStart_date(),
                        event.getEnd_date(),
                        event.getDescription(),
                        event.isPublicEvent(),
                        event.getCreate_date(),
                        event.getUpdate_date()
                ))
                .toList();
    }

    public List<EventResponse> getAllEditorCreatorEvents() {
        UserEntity user = getCurrentUser();
        List<EventRole> roles = List.of(EventRole.CREATOR, EventRole.EDITOR);
        return eventRepository.findEventsByUserAndRole(user, roles)
                .stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getName(),
                        event.getLocation(),
                        event.getStart_date(),
                        event.getEnd_date(),
                        event.getDescription(),
                        event.isPublicEvent(),
                        event.getCreate_date(),
                        event.getUpdate_date()
                ))
                .toList();
    }

    public Optional<EventEntity> findByName(String name) {
        return eventRepository.findByName(name);
    }

    private UserEntity getCurrentUser() {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public void checkPermission(EventEntity event, UserEntity user, EventRole... allowedRoles) {
        Optional<UserEventRoleEntity> roleOpt = userEventRoleRepository.findByUserAndEvent(user, event);

        if (roleOpt.isEmpty() || Arrays.stream(allowedRoles).noneMatch(role -> role == roleOpt.get().getRole())) {
            throw new AccessDeniedException("You do not have permission for this action");
        }
    }
}
