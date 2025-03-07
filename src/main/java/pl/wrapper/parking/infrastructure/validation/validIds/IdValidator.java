package pl.wrapper.parking.infrastructure.validation.validIds;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

class IdValidator implements ConstraintValidator<ValidIds, List<Integer>> {

    private static final int[] ALLOWED_IDS = {1, 2, 3, 4, 5};

    @Override
    public boolean isValid(List<Integer> requestList, ConstraintValidatorContext constraintValidatorContext) {
        return requestList == null || allMatch(requestList);
    }

    private static boolean allMatch(List<Integer> toCheck) {
        for (int id : toCheck) {
            boolean isValid = false;
            for (int allowedId : ALLOWED_IDS) {
                if (id == allowedId) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) return false;
        }
        return true;
    }
}
