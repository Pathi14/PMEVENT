package fr.pmevent.service;

import fr.pmevent.dto.EventDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.exception.AlreadyExistsException;
import fr.pmevent.mapper.EventMapper;
import fr.pmevent.repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public void createEvent(EventDto eventDto) {
        Optional<EventEntity> existingEvent = findByName(eventDto.getName());
        if (existingEvent.isPresent()) {
            throw new AlreadyExistsException("Event already exists");
        }

        eventRepository.save(eventMapper.toEntity(eventDto));
    }

    public Optional<EventEntity> findByName(String name) {
        return eventRepository.findByName(name);
    }
}
