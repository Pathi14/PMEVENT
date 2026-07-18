package fr.pmevent.common.dto.event;

import fr.pmevent.common.validation.HasDateRange;
import fr.pmevent.common.validation.ValidDateRange;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@ValidDateRange
public class UpdateEventDto implements HasDateRange {

    private String name;
    private String location;
    private LocalDate start_date;
    private LocalDate end_date;
    private String description;
    private boolean publicEvent;
    private MultipartFile image;

}
