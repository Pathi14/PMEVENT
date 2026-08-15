package fr.pmevent.controller;

import fr.pmevent.common.dto.event.CreateEventDto;
import fr.pmevent.common.dto.event.EventResponseDto;
import fr.pmevent.common.dto.event.UpdateEventDto;
import fr.pmevent.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/events")
public class EventController {

    private EventService eventService;

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> findEvent(@PathVariable UUID id) {
        EventResponseDto event = eventService.findEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/public")
    public ResponseEntity<List<EventResponseDto>> getPublicEvents() {
        return ResponseEntity.ok(eventService.getAllPublicEvents());
    }

    @GetMapping("/guest")
    public ResponseEntity<List<EventResponseDto>> getViewerEvents() {
        return ResponseEntity.ok(eventService.getAllViewerEvents());
    }

    @GetMapping("/administrator")
    public ResponseEntity<List<EventResponseDto>> getAllEditorCreatorEvents() {
        return ResponseEntity.ok(eventService.getAllEditorCreatorEvents());
    }

    @PostMapping(
            value = "/new-event",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> createEvent(@ModelAttribute CreateEventDto eventDto) {
        try {
            if (eventDto.getName() == null) {
                return ResponseEntity.badRequest().body("Le nom est obligatoire");
            }
            EventResponseDto event = eventService.createEvent(eventDto);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(
            value = "/update-event/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateEvent(
            @PathVariable UUID id,
            @ModelAttribute UpdateEventDto eventDto
    ) {
        try {
            EventResponseDto event = eventService.updateEvent(id, eventDto);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
