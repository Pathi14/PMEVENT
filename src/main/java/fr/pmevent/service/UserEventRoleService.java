package fr.pmevent.service;

import fr.pmevent.dto.AssignRoleDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.entity.UserEventRoleEntity;
import fr.pmevent.enums.EventRole;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.UserEventRoleRepository;
import fr.pmevent.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Repository
@AllArgsConstructor
public class UserEventRoleService {
    private final UserEventRoleRepository userEventRoleRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Assigne un rôle à un utilisateur pour un évènement.
     * Si un rôle existait déjà, il est mis à jour.
     * Seuls le CREATOR les EDITORs de l'évènement peut attribuer un rôle.
     */
    public void AssignRoleToUSer(String userEmail, AssignRoleDto assignRoleDto) {

        //Rechercher l'utilisateur auquel on veut attribuer le rôle et l'évènement concerné
        UserEntity targetUser = userRepository.findById(assignRoleDto.getUserId())
                .orElseThrow(() -> new RuntimeException("You can assign role if user is not valid"));
        EventEntity event = eventRepository.findById(assignRoleDto.getEventId())
                .orElseThrow(() -> new RuntimeException("You can assign role if event is not valid"));

        //Vérifier que c'est bien le créateur de l'évènement qui veut attribuer un roôle
        UserEntity creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        UserEventRoleEntity currentUserRole = userEventRoleRepository
                .findByUserAndEvent(creator, event)
                .orElseThrow(() -> new RuntimeException("You are not the creator of this event"));

        if (currentUserRole.getRole() != EventRole.CREATOR && currentUserRole.getRole() != EventRole.EDITOR) {
            throw new RuntimeException("Seuls le créateur et les éditeurs peuvent attribuer des rôles");
        }

        // Ne pas modifier le rôle du créateur
        UserEventRoleEntity targetRole = userEventRoleRepository
                .findByUserAndEvent(targetUser, event)
                .orElse(null);

        if (targetRole != null && targetRole.getRole() == EventRole.CREATOR) {
            throw new RuntimeException("Le rôle du créateur de l'évènement ne peut pas être modifié.");
        }

        //Vérifier si l'utilisateur à un rôle alors faire une mise à jour sinon lui attribuer un rôle
        UserEventRoleEntity existingRole = userEventRoleRepository
                .findByUserAndEvent(targetUser, event)
                .orElse(null);

        if (existingRole != null) {
            existingRole.setRole(assignRoleDto.getRole());
            userEventRoleRepository.save(existingRole);
        } else {
            UserEventRoleEntity userEventRole = new UserEventRoleEntity();
            userEventRole.setUser(targetUser);
            userEventRole.setEvent(event);
            userEventRole.setRole(assignRoleDto.getRole());
            userEventRoleRepository.save(userEventRole);
        }
    }

    /**
     * Supprime un rôle attribué à un utilisateur sur un évènement.
     * Seuls le CREATOR et les EDITORs peut le faire.
     */
    public void removeRoleFromUser(String userEmail, Long eventId, Long userId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        UserEntity creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        UserEventRoleEntity currentUserRole = userEventRoleRepository
                .findByUserAndEvent(creator, event)
                .orElseThrow(() -> new RuntimeException("You are not assigned to this event"));

        if (currentUserRole.getRole() != EventRole.CREATOR && currentUserRole.getRole() != EventRole.EDITOR) {
            throw new RuntimeException("Seuls le créateur et les éditeurs peuvent rétirer des rôles");
        }

        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserEventRoleEntity role = userEventRoleRepository
                .findByUserAndEvent(targetUser, event)
                .orElseThrow(() -> new RuntimeException("This user has no role for this event"));

        userEventRoleRepository.delete(role);
    }

    /**
     * Retourne la liste des rôles d’un évènement (visible uniquement pour CREATOR et EDITOR)
     */
    public List<Map<String, Object>> getRolesByEvent(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<UserEventRoleEntity> roles = userEventRoleRepository.findByEvent(event);

        return roles.stream().map(role -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", role.getUser().getId());
            map.put("userEmail", role.getUser().getEmail());
            map.put("role", role.getRole());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Retourne le rôle du user connecté pour un évènement.
     */
    public Map<String, String> getMyRoleForEvent(String userEmail, Long eventId) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        UserEventRoleEntity role = userEventRoleRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new RuntimeException("You are not assigned to this event"));
        return Map.of("role", role.getRole().name());
    }
}
