package fr.pmevent.mapper;

import fr.pmevent.dto.authentication.RegisterDto;
import fr.pmevent.dto.user.UserResponseDto;
import fr.pmevent.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toEntity(RegisterDto dto);

    UserResponseDto toResponse(UserEntity user);
}
