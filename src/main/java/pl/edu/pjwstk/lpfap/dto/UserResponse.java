package pl.edu.pjwstk.lpfap.dto;

import pl.edu.pjwstk.lpfap.model.Role;
import pl.edu.pjwstk.lpfap.model.User;
import java.time.LocalDate;

public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String bio;
    private String avatarUrl;
    private Role role;
    private LocalDate registrationDate;

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.id = u.getId();
        r.email = u.getEmail();
        r.fullName = u.getFullName();
        r.phone = u.getPhone();
        r.bio = u.getBio();
        r.avatarUrl = u.getAvatarUrl();
        r.role = u.getRole();
        r.registrationDate = u.getRegistrationDate();
        return r;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getBio() { return bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public Role getRole() { return role; }
    public LocalDate getRegistrationDate() { return registrationDate; }
}
