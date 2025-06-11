package fr.pmevent.dto;

import fr.pmevent.enums.EventRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleDto {
    @NotNull
    private Long userId;

    @NotNull
    private Long eventId;

    @NotNull
    private EventRole role;
}
