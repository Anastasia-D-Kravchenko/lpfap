package pl.edu.pjwstk.lpfap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shelters")
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String address;

    @Email
    @NotBlank
    @Column(nullable = false, length = 255)
    private String contactEmail;

    @Pattern(regexp = "^\\+?[0-9 \\-()]{7,20}$")
    @NotBlank
    @Column(nullable = false, length = 30)
    private String contactPhone;

    @OneToMany(mappedBy = "shelter", cascade = CascadeType.ALL)
    private List<Animal> animals = new ArrayList<>();

    @OneToMany(mappedBy = "shelter")
    private List<User> administrators = new ArrayList<>();

    public Shelter() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public List<Animal> getAnimals() { return animals; }
    public List<User> getAdministrators() { return administrators; }
}
