package pl.edu.pjwstk.lpfap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.edu.pjwstk.lpfap.model.ApplicationType;

public class ApplicationRequest {

    @NotNull(message = "Application type is required")
    private ApplicationType type;

    @NotBlank(message = "Motivation text is required")
    @Size(min = 50, max = 2000, message = "Motivation text must be 50-2000 characters")
    private String motivationText;

    @NotNull(message = "Animal ID is required")
    private Long animalId;

    public ApplicationType getType() { return type; }
    public void setType(ApplicationType type) { this.type = type; }
    public String getMotivationText() { return motivationText; }
    public void setMotivationText(String motivationText) { this.motivationText = motivationText; }
    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }
}
