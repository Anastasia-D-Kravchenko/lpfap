package pl.edu.pjwstk.lpfap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must be 255 characters or fewer")
    private String fullName;

    @Pattern(regexp = "^\\+?[0-9 \\-()]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Size(max = 2000, message = "Bio must be 2000 characters or fewer")
    private String bio;

    @Size(max = 500, message = "Avatar URL must be 500 characters or fewer")
    private String avatarUrl;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
