package fr.pmevent.service;

import fr.pmevent.common.dto.AssignRoleDto;
import fr.pmevent.common.enums.EventRole;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.entity.UserEventRoleEntity;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.UserEventRoleRepository;
import fr.pmevent.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserEventRoleService {
    private final UserEventRoleRepository userEventRoleRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventPermissionService eventPermissionService;

    public void assignPermissions(String userEmail, AssignRoleDto assignment) {
        UserEntity currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        EventEntity event = eventRepository.findById(assignment.getEventId())
                .orElseThrow(() -> new RuntimeException("Événement introuvable."));

        UserEventRoleEntity currentMembership = userEventRoleRepository.findByUserAndEvent(currentUser, event)
                .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de cet événement."));
        requireRoleManagement(currentMembership);

        UserEntity targetUser = userRepository.findById(assignment.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new RuntimeException("Vous ne pouvez pas modifier vos propres autorisations.");
        }

        UserEventRoleEntity membership = userEventRoleRepository.findByUserAndEvent(targetUser, event)
                .orElseGet(UserEventRoleEntity::new);
        if (membership.getRole() == EventRole.CREATOR) {
            throw new RuntimeException("Les autorisations du créateur ne peuvent pas être modifiées.");
        }

        membership.setUser(targetUser);
        membership.setEvent(event);
        membership.setRole(EventRole.VIEWER);
        membership.setCanModifyEvent(assignment.getCanModifyEvent());
        membership.setCanDeleteEvent(assignment.getCanDeleteEvent());
        membership.setCanManageGuests(assignment.getCanManageGuests());
        membership.setCanManageRoles(assignment.getCanManageRoles());
        userEventRoleRepository.save(membership);
    }

    public void removePermissions(String userEmail, UUID eventId, UUID userId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable."));
        UserEntity currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        UserEventRoleEntity currentMembership = userEventRoleRepository.findByUserAndEvent(currentUser, event)
                .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de cet événement."));
        requireRoleManagement(currentMembership);

        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new RuntimeException("Vous ne pouvez pas retirer vos propres autorisations.");
        }

        UserEventRoleEntity targetMembership = userEventRoleRepository.findByUserAndEvent(targetUser, event)
                .orElseThrow(() -> new RuntimeException("Cet utilisateur n'a aucun accès à l'événement."));
        if (targetMembership.getRole() == EventRole.CREATOR) {
            throw new RuntimeException("Les autorisations du créateur ne peuvent pas être retirées.");
        }

        userEventRoleRepository.delete(targetMembership);
    }

    public List<Map<String, Object>> getPermissionsByEvent(UUID eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable."));
        UserEntity currentUser = getCurrentUser();
        UserEventRoleEntity currentMembership = userEventRoleRepository.findByUserAndEvent(currentUser, event)
                .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de cet événement."));
        requireRoleManagement(currentMembership);

        return userEventRoleRepository.findByEvent(event).stream().map(this::toPermissionResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getMyPermissionsForEvent(String userEmail, UUID eventId) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable."));
        UserEventRoleEntity membership = userEventRoleRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de cet événement."));
        return toPermissionResponse(membership);
    }

    private Map<String, Object> toPermissionResponse(UserEventRoleEntity membership) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", membership.getUser().getId());
        response.put("userName", membership.getUser().getName());
        response.put("userFirstname", membership.getUser().getFirstname());
        response.put("userEmail", membership.getUser().getEmail());
        response.put("role", membership.getRole() == EventRole.CREATOR ? "CREATOR" : "MEMBER");
        response.put("canModifyEvent", eventPermissionService.hasPermission(
                membership, EventPermissionService.Permission.MODIFY_EVENT));
        response.put("canDeleteEvent", eventPermissionService.hasPermission(
                membership, EventPermissionService.Permission.DELETE_EVENT));
        response.put("canManageGuests", eventPermissionService.hasPermission(
                membership, EventPermissionService.Permission.MANAGE_GUESTS));
        response.put("canManageRoles", eventPermissionService.hasPermission(
                membership, EventPermissionService.Permission.MANAGE_ROLES));
        return response;
    }

    private void requireRoleManagement(UserEventRoleEntity membership) {
        if (!eventPermissionService.hasPermission(membership, EventPermissionService.Permission.MANAGE_ROLES)) {
            throw new RuntimeException("Vous n'avez pas l'autorisation de gérer les accès de cet événement.");
        }
    }

    private UserEntity getCurrentUser() {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
    }
}
