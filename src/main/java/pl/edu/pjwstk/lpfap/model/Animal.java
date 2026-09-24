package pl.edu.pjwstk.lpfap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "animals")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Species species;

    @Column(length = 100)
    private String breed;

    @Min(0)
    @Column(nullable = false)
    private Integer age;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sex sex;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(columnDefinition = "CLOB")
    private String medicalStatus;

    @Column(length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnimalStatus status = AnimalStatus.AVAILABLE;

    @Column(nullable = false)
    private LocalDate intakeDate = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_id", nullable = false)
    private Shelter shelter;

    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    public Animal() {}

    public void archive() {
        this.status = AnimalStatus.ARCHIVED;
    }

    public void markAdopted() {
        this.status = AnimalStatus.ADOPTED;
    }

    public void markFostered() {
        this.status = AnimalStatus.FOSTERED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public AnimalStatus getStatus() { return status; }
    public void setStatus(AnimalStatus status) { this.status = status; }
    public LocalDate getIntakeDate() { return intakeDate; }
    public void setIntakeDate(LocalDate intakeDate) { this.intakeDate = intakeDate; }
    public Shelter getShelter() { return shelter; }
    public void setShelter(Shelter shelter) { this.shelter = shelter; }
    public List<Application> getApplications() { return applications; }
}
