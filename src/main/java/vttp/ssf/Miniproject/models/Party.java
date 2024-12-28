package vttp.ssf.Miniproject.models;

import java.util.List;

public class Party {

    private String id;       // Unique ID for the party
    private String name;     // Party name
    private String date;     // Party date
    private String time;     // Party time
    private String location; // Optional: Location
    private String userEmail; // Link the party to the user who created it
    private List<Guest> guests; // List of guests attending the party
    private List<Details> drinks; // List of drinks for that party

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(List<Guest> guests) {
        this.guests = guests;
    }

    public List<Details> getDrinks() {
        return drinks;
    }

    public void setDrinks(List<Details> drinks) {
        this.drinks = drinks;
    }
}
