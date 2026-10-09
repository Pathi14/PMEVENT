package fr.pmevent.service;

import fr.pmevent.common.enums.EventRole;
import fr.pmevent.entity.UserEventRoleEntity;
import fr.pmevent.repository.UserEventRoleRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class EventPermissionServiceTest {
    private final EventPermissionService permissions =
            new EventPermissionService(mock(UserEventRoleRepository.class));

    @Test
    void usesExplicitPermissionsForAssignedMembers() {
        UserEventRoleEntity membership = membership(EventRole.VIEWER);
        membership.setCanModifyEvent(true);
        membership.setCanDeleteEvent(false);
        membership.setCanManageGuests(false);
        membership.setCanManageRoles(true);

        assertTrue(permissions.hasPermission(membership, EventPermissionService.Permission.MODIFY_EVENT));
        assertFalse(permissions.hasPermission(membership, EventPermissionService.Permission.DELETE_EVENT));
        assertFalse(permissions.hasPermission(membership, EventPermissionService.Permission.MANAGE_GUESTS));
        assertTrue(permissions.hasPermission(membership, EventPermissionService.Permission.MANAGE_ROLES));
    }

    @Test
    void preservesLegacyEditorPermissionsUntilTheyAreReconfigured() {
        UserEventRoleEntity membership = membership(EventRole.EDITOR);

        assertTrue(permissions.hasPermission(membership, EventPermissionService.Permission.MODIFY_EVENT));
        assertFalse(permissions.hasPermission(membership, EventPermissionService.Permission.DELETE_EVENT));
        assertTrue(permissions.hasPermission(membership, EventPermissionService.Permission.MANAGE_GUESTS));
        assertTrue(permissions.hasPermission(membership, EventPermissionService.Permission.MANAGE_ROLES));
    }

    @Test
    void creatorAlwaysHasAllPermissions() {
        UserEventRoleEntity membership = membership(EventRole.CREATOR);
        membership.setCanModifyEvent(false);
        membership.setCanDeleteEvent(false);
        membership.setCanManageGuests(false);
        membership.setCanManageRoles(false);

        for (EventPermissionService.Permission permission : EventPermissionService.Permission.values()) {
            assertTrue(permissions.hasPermission(membership, permission));
        }
    }

    @Test
    void anySingleAdministrativePermissionMakesEventAdministrable() {
        for (EventPermissionService.Permission permission : EventPermissionService.Permission.values()) {
            UserEventRoleEntity membership = membership(EventRole.VIEWER);
            grantOnly(membership, permission);

            assertTrue(permissions.hasAnyAdministrativePermission(membership), permission.name());
        }
    }

    @Test
    void noAdministrativePermissionsKeepsEventOutOfAdminList() {
        UserEventRoleEntity membership = membership(EventRole.VIEWER);
        membership.setCanModifyEvent(false);
        membership.setCanDeleteEvent(false);
        membership.setCanManageGuests(false);
        membership.setCanManageRoles(false);

        assertFalse(permissions.hasAnyAdministrativePermission(membership));
    }

    private UserEventRoleEntity membership(EventRole role) {
        UserEventRoleEntity membership = new UserEventRoleEntity();
        membership.setRole(role);
        return membership;
    }

    private void grantOnly(UserEventRoleEntity membership, EventPermissionService.Permission permission) {
        membership.setCanModifyEvent(permission == EventPermissionService.Permission.MODIFY_EVENT);
        membership.setCanDeleteEvent(permission == EventPermissionService.Permission.DELETE_EVENT);
        membership.setCanManageGuests(permission == EventPermissionService.Permission.MANAGE_GUESTS);
        membership.setCanManageRoles(permission == EventPermissionService.Permission.MANAGE_ROLES);
    }
}
