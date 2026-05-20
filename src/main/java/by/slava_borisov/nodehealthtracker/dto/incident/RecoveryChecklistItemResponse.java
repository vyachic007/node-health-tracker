package by.slava_borisov.nodehealthtracker.dto.incident;

public record RecoveryChecklistItemResponse(

        Integer stepNumber,

        String title,

        String description,

        Boolean isCritical
) {
}