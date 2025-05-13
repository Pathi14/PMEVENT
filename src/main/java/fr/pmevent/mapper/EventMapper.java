package fr.pmevent.mapper;

import fr.pmevent.dto.CreateEventDto;
import fr.pmevent.dto.UpdateEventDto;
import fr.pmevent.entity.EventEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventEntity toEntity(CreateEventDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromDto(UpdateEventDto dto, @MappingTarget EventEntity entity);
}
