package pl.wrapper.parking.infrastructure.validation.validIds;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIds {
    String message() default "One or more of provided ID values is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
