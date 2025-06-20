package fr.pmevent.controller;

import fr.pmevent.dto.event.CreateEventDto;
import fr.pmevent.dto.event.UpdateEventDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.service.EventService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/events")
public class EventController {

    private EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventEntity>> getAllEvent() {
        return ResponseEntity.ok(eventService.getAllEvent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findEvent(@PathVariable long id) {
        EventEntity event = eventService.findEventById(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/new-event")
    public ResponseEntity<String> createEvent(@RequestBody @Valid CreateEventDto eventDto) {
        try {
            if (eventDto.getName() == null) {
                return ResponseEntity.badRequest().body("name event is empty");
            }
            eventService.createEvent(eventDto);
            return ResponseEntity.ok("Event successfully created : " + eventDto.getName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-event/{id}")
    public ResponseEntity<String> updateEvent(@PathVariable long id, @RequestBody @Valid UpdateEventDto eventDto) {
        eventService.updateEvent(id, eventDto);
        return ResponseEntity.ok("Event sucessfully updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok("Event successfully deleted");
    }
}
