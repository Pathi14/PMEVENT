package fr.pmevent.service;

import fr.pmevent.dto.CreateEventDto;
import fr.pmevent.dto.UpdateEventDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.entity.UserEventRoleEntity;
import fr.pmevent.enums.EventRole;
import fr.pmevent.exception.AlreadyExistsException;
import fr.pmevent.mapper.EventMapper;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.UserEventRoleRepository;
import fr.pmevent.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final UserEventRoleRepository userEventRoleRepository;

    public List<EventEntity> getAllEvent() {
        return eventRepository.findAll();
    }

    public void createEvent(CreateEventDto eventDto) {
        Optional<EventEntity> existingEvent = findByName(eventDto.getName());
        if (existingEvent.isPresent()) {
            throw new AlreadyExistsException("Event already exists");
        }

        EventEntity event = eventMapper.toEntity(eventDto);
        eventRepository.save(event);

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserEventRoleEntity userEventRole = new UserEventRoleEntity();
        userEventRole.setUser(user);
        userEventRole.setEvent(event);
        userEventRole.setRole(EventRole.CREATOR);

        userEventRoleRepository.save(userEventRole);
    }

    public void deleteEvent(long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityExistsException("This event doesn't exists");
        }

        eventRepository.deleteById(id);
    }

    public void updateEvent(long id, UpdateEventDto updateEvent) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("This event doesn't exists"));

        eventMapper.updateEventFromDto(updateEvent, event);
        eventRepository.save(event);
    }

    public Optional<EventEntity> findByName(String name) {
        return eventRepository.findByName(name);
    }
}
