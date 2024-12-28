package vttp.ssf.Miniproject.models;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.context.MessageSource;

import java.util.List;

public class Guest {

    private String id;
    @NotEmpty(message = "Please enter a name")
    private String name;
    @NotEmpty(message = "Please indicate whether the guest is coming to the party")
    private String rsvp;
    @NotEmpty(message = "Please indicate if u have any preference")
    private List<String> preferences;

    public Guest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRsvp() {
        return rsvp;
    }

    public void setRsvp(String rsvp) {
        this.rsvp = rsvp;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }
}
