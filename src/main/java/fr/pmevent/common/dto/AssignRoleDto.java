package fr.pmevent.common.dto;

import fr.pmevent.common.enums.EventRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignRoleDto {
    @NotNull
    private UUID userId;

    @NotNull
    private UUID eventId;

    @NotNull
    private EventRole role;
}
