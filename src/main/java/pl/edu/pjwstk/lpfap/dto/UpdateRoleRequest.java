package pl.edu.pjwstk.lpfap.dto;

import jakarta.validation.constraints.NotNull;
import pl.edu.pjwstk.lpfap.model.Role;

public class UpdateRoleRequest {
    @NotNull
    private Role role;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
