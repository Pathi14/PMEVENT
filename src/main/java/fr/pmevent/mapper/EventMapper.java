package fr.pmevent.mapper;

import fr.pmevent.dto.event.CreateEventDto;
import fr.pmevent.dto.event.EventResponse;
import fr.pmevent.dto.event.UpdateEventDto;
import fr.pmevent.entity.EventEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventEntity toEntity(CreateEventDto dto);

    EventResponse toResponse(EventEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromDto(UpdateEventDto dto, @MappingTarget EventEntity entity);
}
