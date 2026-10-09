package fr.pmevent.service;

import fr.pmevent.common.enums.EventRole;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.entity.UserEventRoleEntity;
import fr.pmevent.repository.UserEventRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventPermissionService {
    private final UserEventRoleRepository userEventRoleRepository;

    public boolean hasPermission(EventEntity event, UserEntity user, Permission permission) {
        return userEventRoleRepository.findByUserAndEvent(user, event)
                .map(membership -> hasPermission(membership, permission))
                .orElse(false);
    }

    public boolean hasPermission(UserEventRoleEntity membership, Permission permission) {
        if (membership.getRole() == EventRole.CREATOR) return true;

        Boolean explicitPermission = switch (permission) {
            case MODIFY_EVENT -> membership.getCanModifyEvent();
            case DELETE_EVENT -> membership.getCanDeleteEvent();
            case MANAGE_GUESTS -> membership.getCanManageGuests();
            case MANAGE_ROLES -> membership.getCanManageRoles();
        };

        if (explicitPermission != null) return explicitPermission;
        if (membership.getRole() != EventRole.EDITOR) return false;

        return permission != Permission.DELETE_EVENT;
    }

    public boolean hasAnyAdministrativePermission(UserEventRoleEntity membership) {
        return hasPermission(membership, Permission.MODIFY_EVENT)
                || hasPermission(membership, Permission.DELETE_EVENT)
                || hasPermission(membership, Permission.MANAGE_GUESTS)
                || hasPermission(membership, Permission.MANAGE_ROLES);
    }

    public void requirePermission(EventEntity event, UserEntity user, Permission permission) {
        if (!hasPermission(event, user, permission)) {
            throw new AccessDeniedException("You do not have permission for this action");
        }
    }

    public enum Permission {
        MODIFY_EVENT,
        DELETE_EVENT,
        MANAGE_GUESTS,
        MANAGE_ROLES
    }
}
