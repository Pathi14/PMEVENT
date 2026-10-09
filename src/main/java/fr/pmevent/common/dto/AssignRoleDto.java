package fr.pmevent.common.dto;

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
    private Boolean canModifyEvent;

    @NotNull
    private Boolean canDeleteEvent;

    @NotNull
    private Boolean canManageGuests;

    @NotNull
    private Boolean canManageRoles;
}
