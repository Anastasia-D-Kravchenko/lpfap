package pl.edu.pjwstk.lpfap.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.edu.pjwstk.lpfap.model.Sex;
import pl.edu.pjwstk.lpfap.model.Species;

public class AnimalRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String name;

    @NotNull(message = "Species is required")
    private Species species;

    @Size(max = 100, message = "Breed must be 100 characters or fewer")
    private String breed;

    @Min(value = 0, message = "Age must be 0 or greater")
    @Max(value = 600, message = "Age (months) must be 600 or fewer")
    @NotNull(message = "Age is required")
    private Integer age;

    @NotNull(message = "Sex is required")
    private Sex sex;

    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    @Size(max = 2000, message = "Medical status must be 2000 characters or fewer")
    private String medicalStatus;

    @Size(max = 500, message = "Photo URL must be 500 characters or fewer")
    private String photoUrl;

    private Long shelterId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Sex getSex() { return sex; }
    public void setSex(Sex sex) { this.sex = sex; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMedicalStatus() { return medicalStatus; }
    public void setMedicalStatus(String medicalStatus) { this.medicalStatus = medicalStatus; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Long getShelterId() { return shelterId; }
    public void setShelterId(Long shelterId) { this.shelterId = shelterId; }
}
