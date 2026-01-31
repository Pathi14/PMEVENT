package fr.pmevent.service;

import fr.pmevent.dto.guest.AddGuestDto;
import fr.pmevent.dto.guest.GuestResponse;
import fr.pmevent.dto.guest.UpdateGuestDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import fr.pmevent.mapper.GuestMapper;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.GuestRepository;
import fr.pmevent.util.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GuestService {
    private GuestRepository guestRepository;
    private EventRepository eventRepository;
    private GuestMapper guestMapper;
    private final MailService mailService;

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

    public GuestEntity getGuestEntity(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("This guest doesn't exist"));
    }

    public GuestResponse addGuest(Long eventId, AddGuestDto guestDto) {

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("You cannot add guest if the event does not exist."));

        GuestEntity guest = mapToEntity(guestDto, event);

        guest.setQrCodeToken(UUID.randomUUID().toString());

        // Vérifier si une photo est envoyée
        if (guestDto.getPhoto() != null && !guestDto.getPhoto().isEmpty()) {

            String contentType = guestDto.getPhoto().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Le fichier doit être une image (PNG, JPG, JPEG).");
            }

            try {
                if (guest.getPhotoUrl() != null) {
                    Path oldImagePath = Paths.get("uploads/guests")
                            .resolve(Paths.get(guest.getPhotoUrl()).getFileName().toString());
                    Files.deleteIfExists(oldImagePath);
                }

                String fileName = UUID.randomUUID() + "_" + guestDto.getPhoto().getOriginalFilename();
                Path uploadPath = Paths.get("uploads/guests");

                Files.createDirectories(uploadPath);

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(guestDto.getPhoto().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                guest.setPhotoUrl("/uploads/guests/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Erreur lors du chargement de la photo", e);
            }
        }

        guestRepository.save(guest);
        if (guestDto.getSendMail() != null && guestDto.getSendMail()) {
            mailService.sendGuestInvitationEmail(guest, event);
        }
        return guestMapper.toResponse(guest);
    }

    public GuestResponse updateGuest(Long id, UpdateGuestDto guestDto) {
        GuestEntity guest = guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("this guest does not exist"));

        updateFields(guestDto, guest);
        GuestEntity guestUpdated = guestRepository.save(guest);

        return guestMapper.toResponse(guestUpdated);
    }

    public void removeGuest(Long id) {
        guestRepository.deleteById(id);
    }

    public void sendReminderEmail(Long guestId) {
        GuestEntity guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Invité introuvable"));

        EventEntity event = guest.getEvent();

        mailService.sendGuestReminderEmail(guest, event);
    }

    public Map<String, Object> verifyQrCode(String code) {
        String[] parts = code.split(":");
        Long guestId = Long.valueOf(parts[1]);
        String token = parts[2];

        GuestEntity guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Invité introuvable"));

        if (!guest.getQrCodeToken().equals(token))
            throw new RuntimeException("QR Code invalide");

        return Map.of(
                "valid", true,
                "guest", guestMapper.toResponse(guest)
        );
    }

    public void markPresent(Long id) {
        GuestEntity guest = guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invité introuvable"));

        guest.setPresent(true);
        guestRepository.save(guest);
    }

    private static GuestEntity mapToEntity(AddGuestDto guestDto, EventEntity event) {
        GuestEntity guest = new GuestEntity();
        guest.setEvent(event);
        guest.setName(guestDto.getName().toUpperCase());
        guest.setFirstname(StringUtils.capitalizeWords(guestDto.getFirstname()));
        guest.setEmail(guestDto.getEmail());
        guest.setPhone(guestDto.getPhone());
        guest.setNumber_places(guestDto.getNumber_places());
        guest.setComment(guestDto.getComment());
        return guest;
    }


    private static void updateFields(UpdateGuestDto guestDto, GuestEntity guest) {
        if (guestDto.getName() != null && !guestDto.getName().isBlank()) {
            guest.setName(guestDto.getName().toUpperCase());
        }
        if (guestDto.getFirstname() != null && !guestDto.getFirstname().isBlank()) {
            guest.setFirstname(StringUtils.capitalizeWords(guestDto.getFirstname()));
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
        if (guestDto.getPhoto() != null && !guestDto.getPhoto().isEmpty()) {
            try {
                if (guest.getPhotoUrl() != null) {
                    Path oldImagePath = Paths.get("uploads/guests")
                            .resolve(Paths.get(guest.getPhotoUrl()).getFileName().toString());
                    Files.deleteIfExists(oldImagePath);
                }

                String fileName = UUID.randomUUID() + "_" + guestDto.getPhoto().getOriginalFilename();
                Path uploadPath = Paths.get("uploads/guests");

                Files.createDirectories(uploadPath);
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(guestDto.getPhoto().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                guest.setPhotoUrl("/uploads/guests/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Erreur lors du chargement de la photo", e);
            }
        }
    }
}
