package fr.pmevent.service;

import fr.pmevent.dto.guest.AddGuestDto;
import fr.pmevent.dto.guest.GuestResponse;
import fr.pmevent.dto.guest.UpdateGuestDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import fr.pmevent.mapper.GuestMapper;
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
    private GuestMapper guestMapper;

    public List<GuestResponse> getAllGuestOfOneEvent(Long eventId) {
        List<GuestEntity> guests = guestRepository.findByEventId(eventId);
        return guests.stream()
                .map(guestMapper::toResponse)
                .toList();
    }

    public GuestResponse getGuestById(Long id) {
        GuestEntity guest = guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("This guest doesn't exit."));
        return guestMapper.toResponse(guest);
    }

    public GuestResponse addGuest(Long eventId, AddGuestDto guestDto) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("You cannot add guest if the event does not exist."));

        GuestEntity guest = mapToEntity(guestDto, event);
        guestRepository.save(guest);

        return guestMapper.toResponse(guest);
    }

    public GuestResponse updateGuest(Long id, @Valid UpdateGuestDto guestDto) {
        GuestEntity guest = guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("this guest does not exist"));

        updateFields(guestDto, guest);
        GuestEntity guestUpdated = guestRepository.save(guest);

        return guestMapper.toResponse(guestUpdated);
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
