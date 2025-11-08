package fr.pmevent.mapper;

import fr.pmevent.dto.guest.GuestResponse;
import fr.pmevent.entity.GuestEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GuestMapper {
    GuestResponse toResponse(GuestEntity entity);
}
