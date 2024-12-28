package vttp.ssf.Miniproject.models;

import java.util.Objects;

public class Cocktail {
    private String name;        // Maps to "strDrink"
    private String thumbnail;   // Maps to "strDrinkThumb"
    private String idDrink;     // Maps to "idDrink"

    public Cocktail(String name, String thumbnail, String idDrink) {
        this.name = name;
        this.thumbnail = thumbnail;
        this.idDrink = idDrink;
    }

    public String getName() {
        return name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getIdDrink() {
        return idDrink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cocktail cocktail = (Cocktail) o;
        return idDrink.equals(cocktail.idDrink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDrink);
    }
}
