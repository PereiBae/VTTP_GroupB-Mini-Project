package vttp.ssf.Miniproject.models;

import jakarta.validation.constraints.NotEmpty;

public class UserLogin {

    @NotEmpty(message = "Username or email is required")
    private String identifier;

    @NotEmpty(message = "Password is required")
    private String password;

    public UserLogin() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
