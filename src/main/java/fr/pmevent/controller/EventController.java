package fr.pmevent.controller;

import fr.pmevent.dto.event.CreateEventDto;
import fr.pmevent.dto.event.EventResponse;
import fr.pmevent.dto.event.UpdateEventDto;
import fr.pmevent.service.EventService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/events")
public class EventController {

    private EventService eventService;

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> findEvent(@PathVariable long id) {
        EventResponse event = eventService.findEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/public")
    public ResponseEntity<List<EventResponse>> getPublicEvents() {
        return ResponseEntity.ok(eventService.getAllPublicEvents());
    }

    @GetMapping("/guest")
    public ResponseEntity<List<EventResponse>> getViewerEvents() {
        return ResponseEntity.ok(eventService.getAllViewerEvents());
    }

    @GetMapping("/administrator")
    public ResponseEntity<List<EventResponse>> getAllEditorCreatorEvents() {
        return ResponseEntity.ok(eventService.getAllEditorCreatorEvents());
    }

    @PostMapping("/new-event")
    public ResponseEntity<?> createEvent(@RequestBody @Valid CreateEventDto eventDto) {
        try {
            if (eventDto.getName() == null) {
                return ResponseEntity.badRequest().body("Le nom est obligatoire");
            }
            EventResponse event = eventService.createEvent(eventDto);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update-event/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable long id, @RequestBody @Valid UpdateEventDto eventDto) {
        try {
            if (eventDto.getName() == null) {
                return ResponseEntity.badRequest().body("Le nom est obligatoire");
            }
            EventResponse event = eventService.updateEvent(id, eventDto);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
