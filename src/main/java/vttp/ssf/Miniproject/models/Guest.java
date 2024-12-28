package vttp.ssf.Miniproject.models;

import java.util.List;

public class Guest {

    private String id;
    private String name;
    private String rsvp;
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
