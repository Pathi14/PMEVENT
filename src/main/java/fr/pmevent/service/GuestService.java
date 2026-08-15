package fr.pmevent.service;

import fr.pmevent.common.dto.guest.AddGuestDto;
import fr.pmevent.common.dto.guest.GuestResponse;
import fr.pmevent.common.dto.guest.GuestRsvpResponse;
import fr.pmevent.common.dto.guest.UpdateGuestDto;
import fr.pmevent.common.utils.StringUtils;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import fr.pmevent.mapper.GuestMapper;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.GuestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final EventRepository eventRepository;
    private final GuestMapper guestMapper;
    private final MailService mailService;
    private final CloudinaryService cloudinaryService;

    /**
     * Récupérer tous les invités d'un événement
     */
    public List<GuestResponse> getAllGuestOfOneEvent(UUID eventId) {
        List<GuestEntity> guests = guestRepository.findByEventId(eventId);
        return guests.stream().map(guestMapper::toResponse).toList();
    }

    /**
     * Récupérer un invité par son ID
     */
    public GuestResponse getGuestById(UUID id) {
        GuestEntity guest = guestRepository.findById(id).orElseThrow(() -> new RuntimeException("This guest doesn't exist."));
        return guestMapper.toResponse(guest);
    }

    /**
     * Récupérer l'entité Guest
     */
    public GuestEntity getGuestEntity(UUID id) {
        return guestRepository.findById(id).orElseThrow(() -> new RuntimeException("This guest doesn't exist"));
    }

    /**
     * Ajouter un invité
     */
    public GuestResponse addGuest(UUID eventId, AddGuestDto guestDto) {

        EventEntity event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("You cannot add guest if the event does not exist."));
        GuestEntity guest = mapToEntity(guestDto, event);

        guest.setQrCodeToken(UUID.randomUUID().toString());

        /* Permet de supprimer l'image Cloudinary si la sauvegarde BDD échoue. */
        String uploadedPublicId = null;

        try {
            if (guestDto.getPhoto() != null && !guestDto.getPhoto().isEmpty()) {
                validateImage(guestDto.getPhoto());
                Map<?, ?> result = cloudinaryService.uploadImage(guestDto.getPhoto(), "pmevent/guests");
                String secureUrl = (String) result.get("secure_url");
                String publicId = (String) result.get("public_id");

                guest.setPhotoUrl(secureUrl);
                guest.setPhotoPublicId(publicId);

                uploadedPublicId = publicId;
            }

            GuestEntity savedGuest = guestRepository.save(guest);

            /* Envoi du mail uniquement après la sauvegarde réussie. */
            if (Boolean.TRUE.equals(guestDto.getSendMail())) {
                mailService.sendGuestInvitationEmail(savedGuest, event);
            }

            return guestMapper.toResponse(savedGuest);

        } catch (Exception e) {

            /* Si Cloudinary a réussi mais que la BDD échoue, on supprime l'image. */
            if (uploadedPublicId != null) {

                try {
                    cloudinaryService.deleteImage(uploadedPublicId);
                } catch (Exception ignored) {
                    // On ne masque pas l'erreur originale
                }
            }

            throw e;
        }
    }

    /**
     * Modifier un invité
     */
    public GuestResponse updateGuest(UUID id, UpdateGuestDto guestDto) {

        GuestEntity guest = guestRepository.findById(id).orElseThrow(() -> new RuntimeException("This guest does not exist"));

        /* Sauvegarder l'ancien public_id avant toute modification. */
        String oldPublicId = guest.getPhotoPublicId();
        String newPublicId = null;

        try {

            updateFields(guestDto, guest);

            /* Nouvelle photo */
            if (guestDto.getPhoto() != null && !guestDto.getPhoto().isEmpty()) {
                validateImage(guestDto.getPhoto());
                Map<?, ?> result = cloudinaryService.uploadImage(guestDto.getPhoto(), "pmevent/guests");
                String secureUrl = (String) result.get("secure_url");
                newPublicId = (String) result.get("public_id");

                guest.setPhotoUrl(secureUrl);

                guest.setPhotoPublicId(newPublicId);
            }

            /* Sauvegarder la nouvelle version */
            GuestEntity updatedGuest = guestRepository.save(guest);

            /* Une fois la BDD sauvegardée, supprimer l'ancienne image. */
            if (newPublicId != null && oldPublicId != null && !oldPublicId.equals(newPublicId)) {
                cloudinaryService.deleteImage(oldPublicId);
            }

            return guestMapper.toResponse(updatedGuest);

        } catch (Exception e) {

            /*
             * Si la nouvelle image a été uploadée
             * mais que la sauvegarde échoue,
             * supprimer la nouvelle image.
             */
            if (newPublicId != null) {
                try {
                    cloudinaryService.deleteImage(newPublicId);
                } catch (Exception ignored) {
                    // On conserve l'erreur originale
                }
            }

            throw e;
        }
    }

    /**
     * Supprimer un invité
     */
    public void removeGuest(UUID id) {

        GuestEntity guest = guestRepository.findById(id).orElseThrow(() -> new RuntimeException("Invité introuvable"));

        /* Supprimer d'abord l'image Cloudinary */
        if (guest.getPhotoPublicId() != null && !guest.getPhotoPublicId().isBlank()) {
            cloudinaryService.deleteImage(guest.getPhotoPublicId());
        }

        /* Puis supprimer l'invité */
        guestRepository.delete(guest);
    }

    /**
     * Envoyer un rappel par email
     */
    public void sendReminderEmail(UUID guestId) {

        GuestEntity guest = guestRepository.findById(guestId).orElseThrow(() -> new RuntimeException("Invité introuvable"));
        EventEntity event = guest.getEvent();
        mailService.sendGuestReminderEmail(guest, event);
    }

    /**
     * Vérification du QR Code
     */
    public Map<String, Object> verifyQrCode(String code) {

        String[] parts = code.split(":");
        if (parts.length != 3) {
            throw new RuntimeException("QR Code invalide");
        }

        UUID guestId = UUID.fromString(parts[1]);
        String token = parts[2];

        GuestEntity guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Invité introuvable"));

        if (!guest.getQrCodeToken().equals(token)) {
            throw new RuntimeException("QR Code invalide");
        }

        return Map.of("valid", true, "guest", guestMapper.toResponse(guest));
    }

    /**
     * Marquer un invité présent
     */
    public void markPresent(UUID id) {
        GuestEntity guest = guestRepository.findById(id).orElseThrow(() -> new RuntimeException("Invité introuvable"));
        guest.setPresent(true);
        guestRepository.save(guest);
    }

    /**
     * Informations RSVP
     */
    public GuestRsvpResponse getRsvpInfo(String token) {

        GuestEntity guest = guestRepository.findByQrCodeToken(token).orElseThrow(() -> new RuntimeException("Lien invalide ou expiré"));
        EventEntity event = guest.getEvent();

        return new GuestRsvpResponse(
                guest.getId(),
                guest.getName(),
                guest.getFirstname(),
                guest.getNumber_places(),
                guest.getPresent(),
                event.getName(),
                event.getDescription(),
                event.getStart_date(),
                event.getLocation(),
                event.getImageUrl()
        );
    }


    /**
     * Réponse RSVP
     */
    public void updateRsvpResponse(String token, Boolean willAttend) {
        GuestEntity guest = guestRepository.findByQrCodeToken(token).orElseThrow(() -> new RuntimeException("Lien invalide ou expiré"));
        guest.setPresent(willAttend);
        guestRepository.save(guest);
    }


    /**
     * Conversion DTO -> Entity
     */
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


    /**
     * Mise à jour des champs classiques
     */
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
    }


    /**
     * Vérification du fichier image
     */
    private static void validateImage(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Le fichier doit être une image.");
        }
    }
}