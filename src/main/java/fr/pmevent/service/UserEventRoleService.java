package fr.pmevent.service;

import fr.pmevent.dto.AssignRoleDto;
import fr.pmevent.entity.EventEntity;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.entity.UserEventRoleEntity;
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

    public void AssignRoleToUSer(AssignRoleDto assignRoleDto) {
        UserEntity user = userRepository.findById(assignRoleDto.getUserId())
                .orElseThrow(() -> new RuntimeException("You can assign role if user is not valid"));
        EventEntity event = eventRepository.findById(assignRoleDto.getEventId())
                .orElseThrow(() -> new RuntimeException("You can assign role if event is not valid"));

        UserEventRoleEntity userEventRole = new UserEventRoleEntity();

        userEventRole.setUser(user);
        userEventRole.setEvent(event);
        userEventRole.setRole(assignRoleDto.getRole());

        userEventRoleRepository.save(userEventRole);
    }
}
