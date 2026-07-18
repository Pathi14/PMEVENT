package fr.pmevent.mapper;

import fr.pmevent.common.dto.authentication.RegisterDto;
import fr.pmevent.common.dto.user.UserResponseDto;
import fr.pmevent.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toEntity(RegisterDto dto);

    UserResponseDto toResponse(UserEntity user);
}
