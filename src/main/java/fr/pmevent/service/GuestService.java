package fr.pmevent.service;

import fr.pmevent.dto.guest.AddGuestDto;
import fr.pmevent.dto.guest.UpdateGuestDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.GuestRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GuestService {
    private GuestRepository guestRepository;
    private EventRepository eventRepository;

    public List<GuestEntity> getAllGuestOfOneEvent(Long eventId) {
        return guestRepository.findByEventId(eventId);
    }

    public GuestEntity getGuestById(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("This guest doesn't exit."));
    }

    public GuestEntity addGuest(Long eventId, AddGuestDto guestDto) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("You add guest if user is not valid"));

        GuestEntity guest = mapToEntity(guestDto, event);
        guestRepository.save(guest);

        return guest;
    }

    public GuestEntity updateGuest(Long id, @Valid UpdateGuestDto guestDto) {
        GuestEntity guest = guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("this guest does not exist"));

        updateFields(guestDto, guest);
        guestRepository.save(guest);

        return guest;
    }

    public void removeGuest(Long id) {
        guestRepository.deleteById(id);
    }


    private static GuestEntity mapToEntity(AddGuestDto guestDto, EventEntity event) {
        GuestEntity guest = new GuestEntity();
        guest.setEvent(event);
        guest.setName(guestDto.getName());
        guest.setFirstname(guestDto.getFirstname());
        guest.setEmail(guestDto.getEmail());
        guest.setPhone(guestDto.getPhone());
        guest.setNumber_places(guestDto.getNumber_places());
        guest.setComment(guestDto.getComment());
        return guest;
    }


    private static void updateFields(UpdateGuestDto guestDto, GuestEntity guest) {
        if (guestDto.getName() != null && !guestDto.getName().isBlank()) {
            guest.setName(guestDto.getName());
        }
        if (guestDto.getFirstname() != null && !guestDto.getFirstname().isBlank()) {
            guest.setFirstname(guestDto.getFirstname());
        }
        if (guestDto.getEmail() != null && !guestDto.getEmail().isBlank()) {
            guest.setEmail(guestDto.getEmail());
        }
        if (guestDto.getPhone() != null && !guestDto.getPhone().isBlank()) {
            guest.setPhone(guestDto.getPhone());
        }
        if (guestDto.getNumber_places() != null && guestDto.getNumber_places() > 0) {
            guest.setNumber_places(guestDto.getNumber_places());
        }
        if (guestDto.getComment() != null && !guestDto.getComment().isBlank()) {
            guest.setComment(guestDto.getComment());
        }
    }
}
