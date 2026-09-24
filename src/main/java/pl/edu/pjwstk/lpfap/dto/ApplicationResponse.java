package pl.edu.pjwstk.lpfap.dto;

import pl.edu.pjwstk.lpfap.model.Application;
import pl.edu.pjwstk.lpfap.model.ApplicationStatus;
import pl.edu.pjwstk.lpfap.model.ApplicationType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class ApplicationResponse {
    private Long id;
    private ApplicationType type;
    private ApplicationStatus status;
    private String motivationText;
    private LocalDateTime submissionDate;
    private LocalDateTime decisionDate;

    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;
    private String applicantAvatarUrl;
    private String applicantBio;

    private Long animalId;
    private String animalName;
    private String animalPhotoUrl;

    private List<String> photos = Collections.emptyList();

    public static ApplicationResponse from(Application a) { return from(a, Collections.emptyList()); }

    public static ApplicationResponse from(Application a, List<String> photoUrls) {
        ApplicationResponse r = new ApplicationResponse();
        r.id = a.getId();
        r.type = a.getType();
        r.status = a.getStatus();
        r.motivationText = a.getMotivationText();
        r.submissionDate = a.getSubmissionDate();
        r.decisionDate = a.getDecisionDate();
        try {
            if (a.getApplicant() != null) {
                r.applicantId        = a.getApplicant().getId();
                r.applicantName      = a.getApplicant().getFullName();
                r.applicantEmail     = a.getApplicant().getEmail();
                r.applicantPhone     = a.getApplicant().getPhone();
                r.applicantAvatarUrl = a.getApplicant().getAvatarUrl();
                r.applicantBio       = a.getApplicant().getBio();
            }
        } catch (Exception ignore) {  }
        try {
            if (a.getAnimal() != null) {
                r.animalId       = a.getAnimal().getId();
                r.animalName     = a.getAnimal().getName();
                r.animalPhotoUrl = a.getAnimal().getPhotoUrl();
            }
        } catch (Exception ignore) { }
        r.photos = photoUrls == null ? Collections.emptyList() : photoUrls;
        return r;
    }

    public Long getId() { return id; }
    public ApplicationType getType() { return type; }
    public ApplicationStatus getStatus() { return status; }
    public String getMotivationText() { return motivationText; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public LocalDateTime getDecisionDate() { return decisionDate; }
    public Long getApplicantId() { return applicantId; }
    public String getApplicantName() { return applicantName; }
    public String getApplicantEmail() { return applicantEmail; }
    public String getApplicantPhone() { return applicantPhone; }
    public String getApplicantAvatarUrl() { return applicantAvatarUrl; }
    public String getApplicantBio() { return applicantBio; }
    public Long getAnimalId() { return animalId; }
    public String getAnimalName() { return animalName; }
    public String getAnimalPhotoUrl() { return animalPhotoUrl; }
    public List<String> getPhotos() { return photos; }
}
