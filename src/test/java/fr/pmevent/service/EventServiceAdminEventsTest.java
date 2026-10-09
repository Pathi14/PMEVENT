package fr.pmevent.service;

import fr.pmevent.common.enums.EventRole;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.entity.UserEventRoleEntity;
import fr.pmevent.mapper.EventMapper;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.GuestRepository;
import fr.pmevent.repository.UserEventRoleRepository;
import fr.pmevent.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventServiceAdminEventsTest {
    private final EventRepository eventRepository = mock(EventRepository.class);
    private final UserEventRoleRepository userEventRoleRepository = mock(UserEventRoleRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final EventPermissionService eventPermissionService = mock(EventPermissionService.class);
    private final EventMapper eventMapper = mock(EventMapper.class);
    private final UserEntity currentUser = new UserEntity();
    private final EventService eventService = new EventService(
            eventRepository,
            mock(GuestRepository.class),
            eventMapper,
            userRepository,
            userEventRoleRepository,
            mock(CloudinaryService.class),
            eventPermissionService
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void includesEventsCreatedByCurrentUser() {
        EventEntity createdEvent = new EventEntity();
        createdEvent.setId(UUID.randomUUID());
        currentUser.setEmail("creator@example.com");
        authenticateAndStubUser();
        UserEventRoleEntity creatorMembership = new UserEventRoleEntity();
        creatorMembership.setRole(EventRole.CREATOR);
        creatorMembership.setEvent(createdEvent);
        when(userEventRoleRepository.findByUserWithEvent(currentUser)).thenReturn(List.of(creatorMembership));
        when(eventPermissionService.hasAnyAdministrativePermission(creatorMembership)).thenReturn(true);

        assertEquals(1, eventService.getAllEditorCreatorEvents().size());
    }

    @Test
    void includesEventsWhenMemberHasAnyExplicitAdministrativePermission() {
        EventEntity assignedEvent = new EventEntity();
        assignedEvent.setId(UUID.randomUUID());
        UserEventRoleEntity membership = new UserEventRoleEntity();
        membership.setRole(EventRole.VIEWER);
        membership.setEvent(assignedEvent);
        currentUser.setEmail("member@example.com");
        authenticateAndStubUser();
        when(userEventRoleRepository.findByUserWithEvent(currentUser)).thenReturn(List.of(membership));
        when(eventPermissionService.hasAnyAdministrativePermission(membership)).thenReturn(true);

        assertEquals(1, eventService.getAllEditorCreatorEvents().size());
    }

    private void authenticateAndStubUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser.getEmail(), "password")
        );
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(eventMapper.toResponse(any(EventEntity.class))).thenReturn(null);
    }
}
