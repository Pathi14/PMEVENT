package fr.pmevent.mapper;

import fr.pmevent.common.dto.event.CreateEventDto;
import fr.pmevent.common.dto.event.EventResponseDto;
import fr.pmevent.common.dto.event.UpdateEventDto;
import fr.pmevent.entity.EventEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventEntity toEntity(CreateEventDto dto);

    @Mapping(target = "totalGuestPlaces", ignore = true)
    EventResponseDto toResponse(EventEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromDto(UpdateEventDto dto, @MappingTarget EventEntity entity);
}
