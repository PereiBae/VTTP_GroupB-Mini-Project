package vttp.ssf.Miniproject.models;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class UserRegistration {

    @Email(message = "Please use a valid email address")
    @NotEmpty(message = "Please enter your email address")
    private String email;

    @NotEmpty(message = "Your username cannot be empty")
    @Size(min = 4, max =128, message = "Your username must be between 4 and 128 characters")
    private String username;

    @NotEmpty(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = ".*[A-Z].*",
            message = "Password must include at least one uppercase letter"
    )
    @Pattern(
            regexp = ".*\\d.*",
            message = "Password must include at least one number"
    )
    @Pattern(
            regexp = ".*[@$!%*?&].*",
            message = "Password must include at least one special character (@$!%*?&)"
    )
    private String password;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Your birthday must be in the past")
    @NotNull(message = "Please enter your date of birth")
    private Date birthDate;

    public UserRegistration() {
    }

    public UserRegistration(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public UserRegistration(String email, String username, String password, Date birthDate) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}
