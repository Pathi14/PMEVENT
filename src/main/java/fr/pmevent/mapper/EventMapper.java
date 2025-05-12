package fr.pmevent.mapper;

import fr.pmevent.dto.EventDto;
import fr.pmevent.entity.EventEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventEntity toEntity(EventDto dto);
}
