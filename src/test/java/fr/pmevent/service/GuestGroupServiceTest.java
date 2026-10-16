package fr.pmevent.service;

import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import fr.pmevent.entity.GuestGroupEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.mapper.GuestMapper;
import fr.pmevent.repository.EventRepository;
import fr.pmevent.repository.GuestGroupRepository;
import fr.pmevent.repository.GuestRepository;
import fr.pmevent.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestGroupServiceTest {

    @Mock private GuestGroupRepository guestGroupRepository;
    @Mock private GuestRepository guestRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventPermissionService eventPermissionService;
    @Mock private GuestMapper guestMapper;
    @Mock private GuestService guestService;
    @InjectMocks private GuestGroupService guestGroupService;

    private final UUID eventId = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();
    private final UUID guestId = UUID.randomUUID();
    private EventEntity event;
    private GuestGroupEntity group;

    @BeforeEach
    void setUp() {
        event = new EventEntity();
        event.setId(eventId);
        group = new GuestGroupEntity();
        group.setId(groupId);
        group.setEvent(event);
        group.setName("Famille");
        group.setMaxPeople(2);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("owner@example.com", "password", List.of())
        );
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(new UserEntity()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addGuestRejectsAnAssignmentThatWouldExceedCapacity() {
        GuestEntity guest = new GuestEntity();
        guest.setId(guestId);
        guest.setEvent(event);
        guest.setNumber_places(2);
        when(guestGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(guestRepository.sumNumberPlacesByGuestGroupId(groupId)).thenReturn(1L);

        assertThrows(ResponseStatusException.class, () -> guestGroupService.addGuest(groupId, guestId));
        verify(guestRepository, never()).save(guest);
    }

    @Test
    void deletingGroupCanKeepItsGuestsUnassigned() {
        GuestEntity guest = new GuestEntity();
        guest.setId(guestId);
        guest.setGuestGroup(group);
        when(guestGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(guestRepository.findByGuestGroupIdOrderByNameAscFirstnameAsc(groupId)).thenReturn(List.of(guest));

        guestGroupService.deleteGroup(groupId, false);

        verify(guestRepository).saveAll(List.of(guest));
        verify(guestService, never()).removeGuest(any());
        verify(guestGroupRepository).delete(group);
    }

    @Test
    void deletingGroupCanAlsoDeleteItsGuests() {
        GuestEntity guest = new GuestEntity();
        guest.setId(guestId);
        when(guestGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(guestRepository.findByGuestGroupIdOrderByNameAscFirstnameAsc(groupId)).thenReturn(List.of(guest));

        guestGroupService.deleteGroup(groupId, true);

        verify(guestService).removeGuest(guestId);
        verify(guestRepository, never()).saveAll(any());
        verify(guestGroupRepository).delete(group);
    }
}
