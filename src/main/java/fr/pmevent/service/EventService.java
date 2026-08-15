package fr.pmevent.service;

import fr.pmevent.common.dto.event.CreateEventDto;
import fr.pmevent.common.dto.event.EventResponseDto;
import fr.pmevent.common.dto.event.UpdateEventDto;
import fr.pmevent.common.enums.EventRole;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.entity.UserEventRoleEntity;
import fr.pmevent.exception.AlreadyExistsException;
import fr.pmevent.mapper.EventMapper;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.UserEventRoleRepository;
import fr.pmevent.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final UserEventRoleRepository userEventRoleRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Création d'un événement.
     */
    @Transactional
    public EventResponseDto createEvent(CreateEventDto eventDto) {

        if (eventRepository.existsByName(eventDto.getName())) {
            throw new AlreadyExistsException("Un évènement avec ce nom existe déjà.");
        }

        EventEntity event = eventMapper.toEntity(eventDto);
        String uploadedImagePublicId = null;

        try {

            /* Upload de l'image sur Cloudinary */
            if (eventDto.getImage() != null && !eventDto.getImage().isEmpty()) {
                Map<?, ?> uploadResult = cloudinaryService.uploadImage(eventDto.getImage(), "pmevent/events");
                String imageUrl = uploadResult.get("secure_url").toString();
                uploadedImagePublicId = uploadResult.get("public_id").toString();

                event.setImageUrl(imageUrl);
                event.setImagePublicId(uploadedImagePublicId);
            }

            /* Sauvegarde de l'événement */
            EventEntity eventCreated = eventRepository.save(event);

            /* Récupération de l'utilisateur connecté */
            UserEntity user = getCurrentUser();

            /* Création du rôle CREATOR */
            UserEventRoleEntity userEventRole = new UserEventRoleEntity();

            userEventRole.setUser(user);
            userEventRole.setEvent(eventCreated);
            userEventRole.setRole(EventRole.CREATOR);

            userEventRoleRepository.save(userEventRole);

            return eventMapper.toResponse(eventCreated);

        } catch (Exception e) {

            /* Si Cloudinary a déjà reçu l'image mais que la transaction BDD échoue, on supprime l'image. */
            if (uploadedImagePublicId != null) {
                try {
                    cloudinaryService.deleteImage(
                            uploadedImagePublicId
                    );
                } catch (Exception cleanupException) {
                    // On ne masque pas l'erreur originale.
                    // Le fichier pourra être nettoyé ultérieurement.
                }
            }

            throw e;
        }
    }

    /**
     * Modification d'un événement.
     */
    @Transactional
    public EventResponseDto updateEvent(UUID id, UpdateEventDto updateEvent) {

        EventEntity event = eventRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("This event doesn't exist"));
        UserEntity user = getCurrentUser();
        checkPermission(event, user, EventRole.CREATOR, EventRole.EDITOR);

        /* Vérification du nom */
        if (updateEvent.getName() != null && eventRepository.existsByName(updateEvent.getName()) && !updateEvent.getName().equals(event.getName())) {
            throw new AlreadyExistsException(
                    "Un évènement avec ce nom existe déjà."
            );
        }

        /* Sauvegarde des informations de l'ancienne image. On ne la supprime pas immédiatement. */
        String oldImagePublicId = event.getImagePublicId();
        String newImagePublicId = null;

        try {
            /* Mise à jour des champs classiques */
            eventMapper.updateEventFromDto(updateEvent, event);

            /* Nouvelle image */
            if (updateEvent.getImage() != null &&
                    !updateEvent.getImage().isEmpty()) {
                Map<?, ?> uploadResult = cloudinaryService.uploadImage(updateEvent.getImage(), "pmevent/events");
                String newImageUrl = uploadResult.get("secure_url").toString();
                newImagePublicId = uploadResult.get("public_id").toString();

                event.setImageUrl(newImageUrl);
                event.setImagePublicId(newImagePublicId);
            }

            /* Sauvegarde BDD */
            EventEntity eventUpdated = eventRepository.save(event);

            /*  Supprimer l'ancienne image. */
            if (newImagePublicId != null && oldImagePublicId != null && !oldImagePublicId.equals(newImagePublicId)) {
                try {
                    cloudinaryService.deleteImage(oldImagePublicId);
                } catch (Exception cleanupException) {

                    /*
                     * Important :
                     *
                     * On ne fait pas échouer la mise à jour
                     * simplement parce que la suppression
                     * de l'ancienne image Cloudinary a échoué.
                     *
                     * L'ancienne image est alors temporairement
                     * orpheline et pourra être nettoyée.
                     */
                }
            }

            return eventMapper.toResponse(eventUpdated);

        } catch (Exception e) {

            /*
             * Si la nouvelle image a été uploadée mais que
             * la sauvegarde BDD échoue, on supprime la nouvelle
             * image pour éviter de créer un fichier orphelin.
             */
            if (newImagePublicId != null) {

                try {
                    cloudinaryService.deleteImage(newImagePublicId);
                } catch (Exception cleanupException) {
                    // Nettoyage impossible immédiatement.
                    // L'image devra être nettoyée ultérieurement.
                }
            }

            throw e;
        }
    }

    /**
     * Suppression d'un événement.
     */
    @Transactional
    public void deleteEvent(UUID id) {

        EventEntity event = eventRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("This event doesn't exist"));
        UserEntity user = getCurrentUser();
        checkPermission(event, user, EventRole.CREATOR);

        String imagePublicId = event.getImagePublicId();

        /* Suppression des relations associées */
        eventRepository.delete(event);

        /* Suppression de l'image Cloudinary. */
        if (imagePublicId != null) {
            try {
                cloudinaryService.deleteImage(imagePublicId);
            } catch (Exception cleanupException) {

                /*
                 * On ne fait pas échouer la suppression de
                 * l'événement simplement parce que Cloudinary
                 * rencontre une erreur.
                 *
                 * L'image devient temporairement orpheline.
                 */
            }
        }
    }

    /**
     * Recherche d'un événement par son ID.
     */
    public EventResponseDto findEventById(UUID id) {

        EventEntity event = eventRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("This event doesn't exist"));

        /* Les événements publics peuvent être consultés */
        if (!event.isPublicEvent()) {
            UserEntity user = getCurrentUser();
            checkPermission(event, user, EventRole.CREATOR, EventRole.EDITOR, EventRole.VIEWER);
        }

        return new EventResponseDto(
                event.getId(),
                event.getName(),
                event.getLocation(),
                event.getStart_date(),
                event.getEnd_date(),
                event.getDescription(),
                event.isPublicEvent(),
                event.getImageUrl(),
                event.getCreate_date(),
                event.getUpdate_date()
        );
    }

    /**
     * Récupère tous les événements publics.
     */
    public List<EventResponseDto> getAllPublicEvents() {

        List<EventEntity> publicEvents = eventRepository.findByPublicEventTrue();

        return publicEvents.stream().map(eventMapper::toResponse).toList();
    }

    /**
     * Récupère tous les événements auxquels l'utilisateur possède au minimum le rôle VIEWER.
     */
    public List<EventResponseDto> getAllViewerEvents() {

        UserEntity user = getCurrentUser();

        List<EventRole> roles = List.of(
                EventRole.VIEWER,
                EventRole.CREATOR,
                EventRole.EDITOR
        );

        return eventRepository
                .findEventsByUserAndRole(user, roles)
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    /**
     * Récupère les événements auxquels l'utilisateur possède es droits CREATOR ou EDITOR.
     */
    public List<EventResponseDto> getAllEditorCreatorEvents() {

        UserEntity user = getCurrentUser();
        List<EventRole> roles = List.of(
                EventRole.CREATOR,
                EventRole.EDITOR
        );

        return eventRepository
                .findEventsByUserAndRole(user, roles)
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    /**
     * Récupère l'utilisateur actuellement authentifié grâce au JWT.
     */
    private UserEntity getCurrentUser() {

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(currentEmail).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Vérifie que l'utilisateur possède l'un des rôles autorisés pour l'événement.
     */
    public void checkPermission(EventEntity event, UserEntity user, EventRole... allowedRoles) {

        Optional<UserEventRoleEntity> roleOpt = userEventRoleRepository.findByUserAndEvent(user, event);

        if (roleOpt.isEmpty() || Arrays.stream(allowedRoles).noneMatch(role -> role == roleOpt.get().getRole())) {
            throw new AccessDeniedException("You do not have permission for this action");
        }
    }
}
