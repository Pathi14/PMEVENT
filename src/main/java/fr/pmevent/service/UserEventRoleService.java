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

import java.util.List;

@Service
@Repository
@AllArgsConstructor
public class UserEventRoleService {
    private final UserEventRoleRepository userEventRoleRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public List<UserEventRoleEntity> findAllEvents() {
        return userEventRoleRepository.findAll();
    }

    /**
     * Assigne un rôle à un utilisateur pour un évènement.
     * Si un rôle existait déjà, il est mis à jour.
     * Seul le CREATOR de l'évènement peut attribuer un rôle.
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

        UserEventRoleEntity creatorRole = userEventRoleRepository
                .findByUserAndEvent(creator, event)
                .orElseThrow(() -> new RuntimeException("You are not the creator of this event"));

        if (creatorRole.getRole() != EventRole.CREATOR) {
            throw new RuntimeException("Only CREATOR can assign roles");
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
     * Seul le CREATOR peut le faire.
     */
    public void removeRoleFromUser(String userEmail, Long eventId, Long userId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        UserEntity creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        UserEventRoleEntity creatorRole = userEventRoleRepository
                .findByUserAndEvent(creator, event)
                .orElseThrow(() -> new RuntimeException("You are not assigned to this event"));

        if (creatorRole.getRole() != EventRole.CREATOR) {
            throw new RuntimeException("Only CREATOR can remove roles");
        }

        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserEventRoleEntity role = userEventRoleRepository
                .findByUserAndEvent(targetUser, event)
                .orElseThrow(() -> new RuntimeException("This user has no role for this event"));

        userEventRoleRepository.delete(role);
    }
}
