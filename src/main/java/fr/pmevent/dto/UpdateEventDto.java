package fr.pmevent.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEventDto {

    private String name;
    private String location;
    private LocalDate start_date;
    private LocalDate end_date;
    private String description;
}
