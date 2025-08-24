package fr.pmevent.validation;

import java.time.LocalDate;

public interface HasDateRange {
    LocalDate getStart_date();

    LocalDate getEnd_date();
}
