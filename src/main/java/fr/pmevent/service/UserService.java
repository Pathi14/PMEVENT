package fr.pmevent.service;

import fr.pmevent.common.dto.authentication.RegisterDto;
import fr.pmevent.common.dto.user.UpdateUser;
import fr.pmevent.common.dto.user.UserResponseDto;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.mapper.UserMapper;
import fr.pmevent.repository.UserEventRoleRepository;
import fr.pmevent.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserEventRoleRepository userEventRoleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    /**
     * Récupérer tous les utilisateurs
     */
    public List<UserResponseDto> getAllUser() {

        List<UserEntity> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    UserResponseDto dto = new UserResponseDto();

                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setFirstname(user.getFirstname());
                    dto.setEmail(user.getEmail());
                    dto.setCreate_date(user.getCreate_date());
                    dto.setUpdate_date(user.getUpdate_date());

                    return dto;
                })
                .toList();
    }


    /**
     * Récupérer les informations de l'utilisateur connecté
     */
    public UserResponseDto getCurrentUserInfo() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return userMapper.toResponse(user);
    }

    /**
     * Création d'un utilisateur
     */
    public UserEntity createUser(RegisterDto userDto) {

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("This email already exists");
        }

        UserEntity user = new UserEntity();
        user.setName(userDto.getName());
        user.setFirstname(userDto.getFirstname());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        return userRepository.save(user);
    }


    /**
     * Modifier un utilisateur
     */
    public UserResponseDto updateUser(UUID userId, UpdateUser userDto) {

        if (userDto == null) {
            throw new IllegalArgumentException("Le corps de la requête est vide.");
        }

        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("This user does not exist."));

        if (!user.getEmail().equals(connectedUser)) {
            throw new AccessDeniedException("You are not allowed to update this user.");
        }

        String oldPublicId = user.getPhotoPublicId();
        String newPublicId = null;

        try {
            updateFields(userDto, user);

            if (userDto.getPhoto() != null && !userDto.getPhoto().isEmpty()) {
                validateImage(userDto.getPhoto());

                Map<?, ?> result =
                        cloudinaryService.uploadImage(userDto.getPhoto(), "pmevent/users");

                String secureUrl = (String) result.get("secure_url");
                newPublicId = (String) result.get("public_id");

                user.setPhotoUrl(secureUrl);
                user.setPhotoPublicId(newPublicId);
            }

            UserEntity updatedUser = userRepository.save(user);

            if (newPublicId != null && oldPublicId != null && !oldPublicId.equals(newPublicId)) {
                cloudinaryService.deleteImage(oldPublicId);
            }

            return userMapper.toResponse(updatedUser);

        } catch (Exception e) {
            if (newPublicId != null) {
                try {
                    cloudinaryService.deleteImage(newPublicId);
                } catch (Exception ignored) {
                }
            }
            throw e;
        }
    }

    /**
     * Vérifier le mot de passe actuel
     */
    public boolean verifyPassword(UUID id, String currentPassword) {

        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return passwordEncoder.matches(
                currentPassword,
                user.getPassword()
        );
    }


    /**
     * Supprimer un utilisateur
     */
    @Transactional
    public void delete(UUID userId) {

        String connectedUser = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("This user does not exist"));

        /* Vérifier que l'utilisateur supprime bien son propre compte. */
        if (!user.getEmail().equals(connectedUser)) {
            throw new AccessDeniedException("You are not allowed to delete this user.");
        }

        /* Supprimer les relations avec les événements. */
        userEventRoleRepository.deleteByUserId(userId);

        /* Supprimer la photo Cloudinary. */
        if (user.getPhotoPublicId() != null && !user.getPhotoPublicId().isBlank()) {
            cloudinaryService.deleteImage(user.getPhotoPublicId());
        }

        /* Supprimer l'utilisateur. */
        userRepository.delete(user);
    }


    /**
     * Mise à jour des champs utilisateur
     */
    private void updateFields(UpdateUser userDto, UserEntity user) {

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
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
    }


    /**
     * Vérification du fichier image
     */
    private static void validateImage(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Le fichier doit être une image.");
        }
    }
}