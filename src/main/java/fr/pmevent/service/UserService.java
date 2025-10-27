package fr.pmevent.service;

import fr.pmevent.dto.authentication.RegisterDto;
import fr.pmevent.dto.user.UpdateUser;
import fr.pmevent.dto.user.UserResponseDto;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.repository.UserEventRoleRepository;
import fr.pmevent.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserEventRoleRepository userEventRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .firstname(user.getFirstname())
                .email(user.getEmail())
                .create_date(user.getCreate_date())
                .update_date(user.getUpdate_date())
                .build();
    }

    public UserEntity createUser(RegisterDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("This email already exists");
        }
        UserEntity user = new UserEntity();
        user.setName(userDto.getName());
        user.setFirstname(userDto.getFirstname());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());

        return userRepository.save(user);
    }

    public UserEntity updateUser(Long userId, UpdateUser userDto) {
        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("This user does not exist."));

        if (!user.getEmail().equals(connectedUser)) {
            throw new AccessDeniedException("You are not allowed to update this user.");
        }

        updateFields(userDto, user);

        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long userId) {
        String connectedUSer = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("This user does not exist"));

        if (!user.getEmail().equals(connectedUSer)) {
            throw new AccessDeniedException("You are not allowed to update this user.");
        }

        userEventRoleRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
    
    private static void updateFields(UpdateUser userDto, UserEntity user) {
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        if (userDto.getFirstname() != null && !userDto.getFirstname().isBlank()) {
            user.setFirstname(userDto.getFirstname());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(userDto.getPassword());
        }
    }
}
