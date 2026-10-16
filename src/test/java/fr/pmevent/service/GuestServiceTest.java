package fr.pmevent.service;

import fr.pmevent.common.dto.guest.UpdateGuestDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.GuestEntity;
import fr.pmevent.entity.GuestGroupEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.mapper.GuestMapper;
import fr.pmevent.repository.EventRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock private GuestRepository guestRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private GuestMapper guestMapper;
    @Mock private MailService mailService;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private EventPermissionService eventPermissionService;
    @InjectMocks private GuestService guestService;

    @BeforeEach
    void setUp() {
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
    void updatingGuestPlacesUsesGroupOccupancyBeforeApplyingTheNewValue() {
        UUID guestId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        EventEntity event = new EventEntity();
        GuestGroupEntity group = new GuestGroupEntity();
        group.setId(groupId);
        group.setMaxPeople(10);
        GuestEntity guest = new GuestEntity();
        guest.setId(guestId);
        guest.setEvent(event);
        guest.setGuestGroup(group);
        guest.setNumber_places(1);

        UpdateGuestDto request = new UpdateGuestDto();
        request.setNumber_places(2);

        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(guestRepository.sumNumberPlacesByGuestGroupId(groupId))
                .thenAnswer(invocation -> guest.getNumber_places() == 1 ? 9L : 10L);
        when(guestRepository.save(guest)).thenReturn(guest);

        assertDoesNotThrow(() -> guestService.updateGuest(guestId, request));

        verify(guestRepository).sumNumberPlacesByGuestGroupId(groupId);
        verify(guestRepository).save(guest);
    }

    @Test
    void verifyQrRejectsGuestFromAnotherEvent() {
        UUID currentEventId = UUID.randomUUID();
        UUID guestEventId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();
        EventEntity currentEvent = new EventEntity();
        currentEvent.setId(currentEventId);
        EventEntity guestEvent = new EventEntity();
        guestEvent.setId(guestEventId);
        GuestEntity guest = new GuestEntity();
        guest.setId(guestId);
        guest.setEvent(guestEvent);
        guest.setQrCodeToken("valid-token");

        when(eventRepository.findById(currentEventId)).thenReturn(Optional.of(currentEvent));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));

        assertThrows(
                IllegalArgumentException.class,
                () -> guestService.verifyQrCode(currentEventId, "GUEST:" + guestId + ":valid-token")
        );

        verify(guestMapper, never()).toResponse(guest);
    }
}
