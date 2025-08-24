package fr.pmevent.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, HasDateRange> {
    @Override
    public boolean isValid(HasDateRange value, ConstraintValidatorContext context) {
        if (value.getStart_date() == null || value.getEnd_date() == null) {
            return true;
        }
        return !value.getStart_date().isAfter(value.getEnd_date());
    }
}
