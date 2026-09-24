package pl.edu.pjwstk.lpfap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String passwordHash;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(length = 30)
    private String phone;

    @Column(length = 2000)
    private String bio;

    @Column(length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Column(nullable = false)
    private LocalDate registrationDate = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
    public Shelter getShelter() { return shelter; }
    public void setShelter(Shelter shelter) { this.shelter = shelter; }
    public List<Application> getApplications() { return applications; }
}
