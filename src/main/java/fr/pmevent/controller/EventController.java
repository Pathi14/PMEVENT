package fr.pmevent.controller;

import fr.pmevent.dto.EventDto;
import fr.pmevent.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EventController {

    private EventService eventService;

    @GetMapping
    public String health() {
        return "API de gestion d'invitation OK";
    }

    @PostMapping("event")
    public ResponseEntity<String> createEvent(@RequestBody EventDto eventDto) {
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
}
