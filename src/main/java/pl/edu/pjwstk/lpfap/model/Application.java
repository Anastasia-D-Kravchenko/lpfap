package pl.edu.pjwstk.lpfap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ApplicationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @NotBlank
    @Size(min = 50, message = "Motivation text must be at least 50 characters")
    @Column(nullable = false, columnDefinition = "CLOB")
    private String motivationText;

    @Column(nullable = false)
    private LocalDateTime submissionDate = LocalDateTime.now();

    @Column
    private LocalDateTime decisionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    public Application() {}

    public void approve() {
        this.status = ApplicationStatus.APPROVED;
        this.decisionDate = LocalDateTime.now();
    }

    public void reject() {
        this.status = ApplicationStatus.REJECTED;
        this.decisionDate = LocalDateTime.now();
    }

    public void withdraw() {
        this.status = ApplicationStatus.WITHDRAWN;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ApplicationType getType() { return type; }
    public void setType(ApplicationType type) { this.type = type; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public String getMotivationText() { return motivationText; }
    public void setMotivationText(String motivationText) { this.motivationText = motivationText; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }
    public LocalDateTime getDecisionDate() { return decisionDate; }
    public void setDecisionDate(LocalDateTime decisionDate) { this.decisionDate = decisionDate; }
    public User getApplicant() { return applicant; }
    public void setApplicant(User applicant) { this.applicant = applicant; }
    public Animal getAnimal() { return animal; }
    public void setAnimal(Animal animal) { this.animal = animal; }
}
