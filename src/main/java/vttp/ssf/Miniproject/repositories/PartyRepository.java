package vttp.ssf.Miniproject.repositories;

import jakarta.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import vttp.ssf.Miniproject.models.Details;
import vttp.ssf.Miniproject.models.Guest;
import vttp.ssf.Miniproject.models.Party;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class PartyRepository {

    @Autowired
    @Qualifier("redisStringTemplate")
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Fetch data from Redis cache.
     *
     * @param key Redis key to fetch data.
     * @return Cached data or null if not found.
     */
    public List<String> getCachedData(String key) {
        if (redisTemplate.hasKey(key)) {
            List<Object> cachedData = redisTemplate.opsForHash().values(key);
            List<String> data = cachedData.stream().map(Object::toString).collect(Collectors.toList());
            System.out.println("Cached Data for key '" + key + "': " + data);
            return data;
        }
        System.out.println("No cached data for key: " + key);
        return null;
    }

    /**
     * Save data to Redis cache with expiration.
     *
     * @param key  Redis key to save data.
     * @param data Data to cache.
     */
    public void cacheData(String key, List<String> data) {
        if (data == null || data.isEmpty()) {
            System.out.println("No data to cache for key: " + key);
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            redisTemplate.opsForHash().put(key, String.valueOf(i), data.get(i));
        }
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
        System.out.println("Data cached for key: " + key + " -> " + data);
    }

    public void saveDrinkDetails(Details details, String email) {
        JsonArray ingredients = convertToJsonArray(details);
        String drink = Json.createObjectBuilder()
                .add("name", details.getName())
                .add("drinkId", details.getId())
                .add("ingredients", ingredients)
                .add("instructions", details.getInstructions())
                .add("thumbnail", details.getThumbnail())
                .build()
                .toString();
        redisTemplate.opsForHash().put(email, String.valueOf(details.getId()), drink);
    }

    private JsonArray convertToJsonArray(Details details) {
        List<String> ingredients = details.getIngredients();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder(); // Initialize once

        for (String ingredient : ingredients) {
            arrayBuilder.add(ingredient); // Add each ingredient to the builder
        }

        return arrayBuilder.build(); // Build the JsonArray after the loop
    }


    public List<Details> getFavourites(String email) {
        List<Object> rawData = redisTemplate.opsForHash().values(email);
        List<Details> favourites = new ArrayList<>();
        for (Object data : rawData) {
            String jsonString = (String) data;
            JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();

            // Extract fields from the JSON object
            String id = obj.getString("drinkId");
            String name = obj.getString("name");
            JsonArray ingredientsArray = obj.getJsonArray("ingredients");
            String instructions = obj.getString("instructions", null); // Optional field
            String thumbnail = obj.getString("thumbnail", null); // Optional field

            // Convert JSON Array to List<String>
            List<String> ingredientsList = new ArrayList<>();
            for (int j = 0; j < ingredientsArray.size(); j++) {
                ingredientsList.add(ingredientsArray.getString(j));
            }

            // Create Details object
            Details details = new Details();
            details.setName(name);
            details.setId(id);
            details.setIngredients(ingredientsList);
            details.setInstructions(instructions);
            details.setThumbnail(thumbnail);

            // Add to favourites list
            favourites.add(details);
        }

        return favourites;
    }

    public void deleteFavourite(String email, String drinkId) {
        redisTemplate.opsForHash().delete(email, drinkId);
    }

    public List<Object> getGuestList(String email) {
        return redisTemplate.opsForHash().values("guests: " + email);
    }

    public void addGuest(String email, Guest guest) {
        String redisKey = "guests: " + email;
        JsonArray preferences = Json.createArrayBuilder(guest.getPreferences()).build();
        String guestJson = Json.createObjectBuilder()
                .add("id", guest.getId())
                .add("name", guest.getName())
                .add("rsvp", guest.getRsvp())
                .add("preferences", preferences)
                .build()
                .toString();

        System.out.println("Adding guest: " + guest.getName() + " to Redis under key: " + redisKey);
        redisTemplate.opsForHash().put(redisKey, guest.getId(), guestJson);
    }


    public void deleteGuest(String email, String guestId) {
        redisTemplate.opsForHash().delete("guests: " + email, guestId);
    }

    public List<Object> getParties(String email) {
        return redisTemplate.opsForHash().values("parties: " + email);
    }

    public void deleteParty(String email, String id) {
        redisTemplate.opsForHash().delete("parties: " + email, id);
    }

    public String getPartyById(String email, String id) {
        return (String) redisTemplate.opsForHash().get("parties: " + email, id);
    }

    public void saveParty(Party party) {
        String partyJson = Json.createObjectBuilder()
                .add("id", party.getId())
                .add("name", party.getName())
                .add("date", party.getDate())
                .add("time", party.getTime())
                .add("location", party.getLocation())
                .add("userEmail", party.getUserEmail())
                .build()
                .toString();
        redisTemplate.opsForHash().put("parties: " + party.getUserEmail(), party.getId(), partyJson);
    }

    public void saveGuestsToParty(String partyId, List<Guest> guests) {
        String redisKey = "partyGuests:" + partyId; // Use consistent key
        System.out.println("Saving updated guest list to Redis for party: " + partyId);
        redisTemplate.delete(redisKey); // Clear existing data
        JsonArray jsonArray;
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Guest guest : guests) {
            for (int i = 0; i < guest.getPreferences().size(); i++) {
                arrayBuilder.add(guest.getPreferences().get(i));
            }
            jsonArray = arrayBuilder.build();
            String guestJson = Json.createObjectBuilder()
                    .add("id", guest.getId())
                    .add("name", guest.getName())
                    .add("rsvp", guest.getRsvp())
                    .add("preferences", jsonArray)
                    .build()
                    .toString();
            redisTemplate.opsForHash().put(redisKey, guest.getId(), guestJson);
        }
        System.out.println("Updated guests saved to Redis for party: " + partyId);
    }


//    public void saveDrinksToParty(String partyId, List<Details> drinks) {
//        JsonArray drinksArray = Json.createArrayBuilder(
//                drinks.stream()
//                        .map(drink -> Json.createObjectBuilder()
//                                .add("id", drink.getId())
//                                .add("name", drink.getName())
//                                .add("thumbnail", drink.getThumbnail())
//                                .add("ingredients", Json.createArrayBuilder(drink.getIngredients()))
//                                .add("instructions", drink.getInstructions())
//                                .build())
//                        .toList()
//        ).build();
//
//        redisTemplate.opsForHash().put("partyDrinks:" + partyId, "drinks", drinksArray.toString());
//    }

    public List<Guest> getGuestsForParty(String partyId) {
        String redisKey = "partyGuests:" + partyId;
        System.out.println("Fetching guests for party ID: " + redisKey);

        List<Object> rawGuests = redisTemplate.opsForHash().values(redisKey);

        if (rawGuests == null || rawGuests.isEmpty()) {
            System.err.println("No guests found for party ID: " + partyId);
            return new ArrayList<>();
        }

        return rawGuests.stream()
                .map(data -> {
                    String json = (String) data;
                    JsonObject obj = Json.createReader(new StringReader(json)).readObject();
                    Guest guest = new Guest();
                    guest.setId(obj.getString("id"));
                    guest.setName(obj.getString("name"));
                    guest.setRsvp(obj.getString("rsvp"));
                    guest.setPreferences(obj.getJsonArray("preferences").stream()
                            .map(JsonValue::toString)
                            .toList());
                    return guest;
                })
                .toList();
    }


    public List<Details> getDrinksForParty(String partyId) {
        String redisKey = "partyDrinks:" + partyId; // Ensure consistent key format
        List<Object> rawDrinks = redisTemplate.opsForHash().values(redisKey);

        if (rawDrinks == null || rawDrinks.isEmpty()) {
            System.err.println("No drinks found for party ID: " + redisKey);
            return List.of();
        }

        return rawDrinks.stream()
                .map(data -> {
                    String json = (String) data;
                    JsonObject obj = Json.createReader(new StringReader(json)).readObject();

                    Details drink = new Details();
                    drink.setId(obj.getString("id"));
                    drink.setName(obj.getString("name"));
                    drink.setThumbnail(obj.getString("thumbnail"));
                    drink.setIngredients(obj.getJsonArray("ingredients").stream()
                            .map(JsonValue::toString)
                            .map(pref -> pref.replace("\"", "")) // Remove unnecessary quotes
                            .toList());
                    drink.setInstructions(obj.getString("instructions"));
                    return drink;
                })
                .toList();
    }



    public Guest getGuestById(String guestId, String email) {
        String guestJson = (String) redisTemplate.opsForHash().get("guests: " +email, guestId);
        if (guestJson == null) {
            return null; // Guest not found
        }

        JsonObject obj = Json.createReader(new StringReader(guestJson)).readObject();
        Guest guest = new Guest();
        guest.setId(obj.getString("id"));
        guest.setName(obj.getString("name"));
        guest.setRsvp(obj.getString("rsvp"));
        guest.setPreferences(obj.getJsonArray("preferences").stream()
                .map(JsonValue::toString)
                .toList());
        return guest;
    }

    public Details getDrinkDetails(String drinkId, String email) {
        String drinkJson = (String) redisTemplate.opsForHash().get(email, drinkId);
        if (drinkJson == null) {
            return null; // Drink not found
        }

        JsonObject obj = Json.createReader(new StringReader(drinkJson)).readObject();
        Details drink = new Details();
        drink.setId(drinkId);
        drink.setName(obj.getString("name"));
        drink.setThumbnail(obj.getString("thumbnail"));
        drink.setIngredients(obj.getJsonArray("ingredients").stream()
                .map(JsonValue::toString)
                .toList());
        drink.setInstructions(obj.getString("instructions"));
        return drink;
    }

    public void addGuestToParty(String partyId, Guest guest) {
        String redisKey = "partyGuests:" + partyId;
        JsonArray preferences = Json.createArrayBuilder(guest.getPreferences()).build();
        String guestJson = Json.createObjectBuilder()
                .add("id", guest.getId())
                .add("name", guest.getName())
                .add("rsvp", guest.getRsvp())
                .add("preferences", preferences)
                .build()
                .toString();

        System.out.println("Adding guest: " + guest.getName() + " to party ID: " + partyId);
        redisTemplate.opsForHash().put(redisKey, guest.getId(), guestJson);
    }

    public List<Guest> getGuestListForUser(String userEmail) {
        String redisKey = "guests: " + userEmail;
        System.out.println("Fetching guest list for user: " + redisKey);

        List<Object> rawGuests = redisTemplate.opsForHash().values(redisKey);

        if (rawGuests == null || rawGuests.isEmpty()) {
            System.err.println("No guests found for user: " + userEmail);
            return new ArrayList<>();
        }

        return rawGuests.stream()
                .map(data -> {
                    String json = (String) data;
                    JsonObject obj = Json.createReader(new StringReader(json)).readObject();
                    Guest guest = new Guest();
                    guest.setId(obj.getString("id"));
                    guest.setName(obj.getString("name"));
                    guest.setRsvp(obj.getString("rsvp"));
                    guest.setPreferences(obj.getJsonArray("preferences").stream()
                            .map(JsonValue::toString)
                            .toList());
                    return guest;
                })
                .toList();
    }

    public void addDrinkToParty(String partyId, Details drink) {
        String redisKey = "partyDrinks:" + partyId;
        String drinkJson = Json.createObjectBuilder()
                .add("id", drink.getId())
                .add("name", drink.getName())
                .add("thumbnail", drink.getThumbnail())
                .add("ingredients", Json.createArrayBuilder(drink.getIngredients()))
                .add("instructions", drink.getInstructions())
                .build()
                .toString();

        System.out.println("Saving drink to Redis under key: " + redisKey);
        redisTemplate.opsForHash().put(redisKey, drink.getId(), drinkJson);
        System.out.println("Drink added to party: " + partyId);
    }

    public void removeDrinkFromParty(String partyId, String drinkId) {
        String redisKey = "partyDrinks:" + partyId;
        redisTemplate.opsForHash().delete(redisKey, drinkId);
        System.out.println("Drink with ID " + drinkId + " removed from party: " + partyId);
    }


}
