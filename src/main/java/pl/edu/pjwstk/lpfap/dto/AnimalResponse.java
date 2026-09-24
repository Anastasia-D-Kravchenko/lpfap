package pl.edu.pjwstk.lpfap.dto;

import pl.edu.pjwstk.lpfap.model.Animal;
import pl.edu.pjwstk.lpfap.model.AnimalStatus;
import pl.edu.pjwstk.lpfap.model.Sex;
import pl.edu.pjwstk.lpfap.model.Species;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class AnimalResponse {
    private Long id;
    private String name;
    private Species species;
    private String breed;
    private Integer age;
    private Sex sex;
    private String description;
    private String medicalStatus;
    private String photoUrl;
    private List<String> photos = Collections.emptyList();
    private AnimalStatus status;
    private LocalDate intakeDate;
    private String shelterName;

    public static AnimalResponse from(Animal a) { return from(a, Collections.emptyList()); }

    public static AnimalResponse from(Animal a, List<String> galleryUrls) {
        AnimalResponse r = new AnimalResponse();
        r.id = a.getId();
        r.name = a.getName();
        r.species = a.getSpecies();
        r.breed = a.getBreed();
        r.age = a.getAge();
        r.sex = a.getSex();
        r.description = a.getDescription();
        r.medicalStatus = a.getMedicalStatus();
        r.photoUrl = a.getPhotoUrl();
        r.photos = galleryUrls == null ? Collections.emptyList() : galleryUrls;
        r.status = a.getStatus();
        r.intakeDate = a.getIntakeDate();
        try {
            r.shelterName = a.getShelter() != null ? a.getShelter().getName() : null;
        } catch (Exception e) {
            r.shelterName = null;
        }
        return r;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Species getSpecies() { return species; }
    public String getBreed() { return breed; }
    public Integer getAge() { return age; }
    public Sex getSex() { return sex; }
    public String getDescription() { return description; }
    public String getMedicalStatus() { return medicalStatus; }
    public String getPhotoUrl() { return photoUrl; }
    public List<String> getPhotos() { return photos; }
    public AnimalStatus getStatus() { return status; }
    public LocalDate getIntakeDate() { return intakeDate; }
    public String getShelterName() { return shelterName; }
}
