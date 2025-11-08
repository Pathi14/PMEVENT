package fr.pmevent.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "La date de début doit être inférieure à la date de fin";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
