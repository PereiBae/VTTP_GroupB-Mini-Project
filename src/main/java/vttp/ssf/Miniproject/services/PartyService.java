package vttp.ssf.Miniproject.services;

import jakarta.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import vttp.ssf.Miniproject.models.Cocktail;
import vttp.ssf.Miniproject.models.Details;
import vttp.ssf.Miniproject.models.Guest;
import vttp.ssf.Miniproject.models.Party;
import vttp.ssf.Miniproject.repositories.PartyRepository;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PartyService {

    @Autowired
    private PartyRepository partyRepo;

    @Value("${api.url}")
    private String API_URL;

    /**
     * Fetches categories with caching.
     */
    public List<String> getCategories() {
        List<String> categories = partyRepo.getCachedData("categories");
        if (categories != null) {
            System.out.println("Categories retrieved from cache.");
            return categories;
        }

        // Fetch categories from API
        categories = fetchListFromAPI("/list.php?c=list", "strCategory");
        if (!categories.isEmpty()) {
            System.out.println("Caching categories...");
            partyRepo.cacheData("categories", categories);
        }
        return categories;
    }

    /**
     * Fetches ingredients with caching.
     */
    public List<String> getIngredients() {
        List<String> ingredients = partyRepo.getCachedData("ingredients");
        if (ingredients != null) {
            Collections.sort(ingredients);
            System.out.println("Ingredients retrieved from cache.");
            return ingredients;
        }

        // Fetch ingredients from API
        ingredients = fetchListFromAPI("/list.php?i=list", "strIngredient1");
        ingredients = new ArrayList<>(ingredients);
        Collections.sort(ingredients);
        if (!ingredients.isEmpty()) {
            System.out.println("Caching ingredients...");
            partyRepo.cacheData("ingredients", ingredients);
        }
        return ingredients;
    }

    /**
     * Fetches alcoholic filters with caching.
     */
    public List<String> getAlcoholicFilters() {
        List<String> alcoholicFilters = partyRepo.getCachedData("alcoholicFilters");
        if (alcoholicFilters != null) {
            System.out.println("Alcoholic filters retrieved from cache.");
            return alcoholicFilters;
        }

        // Fetch alcoholic filters from API
        alcoholicFilters = fetchListFromAPI("/list.php?a=list", "strAlcoholic");
        if (!alcoholicFilters.isEmpty()) {
            System.out.println("Caching alcoholic filters...");
            partyRepo.cacheData("alcoholicFilters", alcoholicFilters);
        }
        return alcoholicFilters;
    }

    /**
     * Fetches a list of data for dropdowns.
     */
    private List<String> fetchListFromAPI(String endpoint, String fieldKey) {
        String url = UriComponentsBuilder.fromUriString(API_URL + endpoint)
                .build()
                .toUriString();
        System.out.println("API URL: " + url);

        try {
            RequestEntity<Void> request = RequestEntity
                    .get(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .build();

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonReader jsonReader = Json.createReader(new StringReader(response.getBody()));
                JsonObject jsonObject = jsonReader.readObject();

                if (jsonObject.isNull("drinks")) {
                    System.out.println("No drinks found in response.");
                    return List.of();
                }

                JsonArray drinksArray = jsonObject.getJsonArray("drinks");
                return drinksArray.stream()
                        .map(item -> item.asJsonObject().getString(fieldKey))
                        .toList();
            }
        } catch (Exception e) {
            System.err.println("Error during API fetch: " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Main search method for filtering cocktails by ingredient, category, and alcoholic filters.
     */
    public List<Cocktail> searchCocktails(String ingredient, String category, String alcoholic) {
        List<Cocktail> ingredientResults = (ingredient != null && !ingredient.isEmpty())
                ? fetchCocktailsByFilter("i", ingredient)
                : null;

        List<Cocktail> categoryResults = (category != null && !category.isEmpty())
                ? fetchCocktailsByFilter("c", category)
                : null;

        List<Cocktail> alcoholicResults = (alcoholic != null && !alcoholic.isEmpty())
                ? fetchCocktailsByFilter("a", alcoholic)
                : null;

        // Debugging individual results
        System.out.println("Ingredient Results: " + (ingredientResults != null ? ingredientResults.size() : 0));
        System.out.println("Category Results: " + (categoryResults != null ? categoryResults.size() : 0));
        System.out.println("Alcoholic Results: " + (alcoholicResults != null ? alcoholicResults.size() : 0));

        List<Cocktail> results = intersectResults(ingredientResults, categoryResults, alcoholicResults);

        // Debugging final results
        System.out.println("Final Results:");
        results.forEach(drink -> System.out.println(
                "Name: " + drink.getName() +
                        ", Thumbnail: " + drink.getThumbnail() +
                        ", ID: " + drink.getIdDrink()
        ));

        return results;
    }


    /**
     * Fetches cocktails by a specific filter type.
     */
    private List<Cocktail> fetchCocktailsByFilter(String filterType, String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return List.of(); // Return an empty list for empty filters
        }

        String url = UriComponentsBuilder.fromUriString(API_URL)
                .path("/filter.php")
                .queryParam(filterType, filterValue)
                .build()
                .toUriString();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonReader reader = Json.createReader(new StringReader(response));
                JsonObject jsonObject = reader.readObject();

                if (jsonObject.containsKey("drinks") && !jsonObject.isNull("drinks")) {
                    JsonArray drinksArray = jsonObject.getJsonArray("drinks");
                    return drinksArray.stream()
                            .map(drink -> {
                                JsonObject obj = drink.asJsonObject();
                                return new Cocktail(
                                        obj.getString("strDrink"),      // Maps to "name"
                                        obj.getString("strDrinkThumb"), // Maps to "thumbnail"
                                        obj.getString("idDrink")        // Maps to "idDrink"
                                );
                            })
                            .toList();
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching data for filter: " + filterType + " = " + filterValue);
            e.printStackTrace();
        }

        return List.of(); // Return an empty list on failure
    }

    /**
     * Finds the intersection of multiple filtered lists.
     */
    @SafeVarargs
    private List<Cocktail> intersectResults(List<Cocktail>... lists) {
        Set<Cocktail> resultSet = null;

        for (List<Cocktail> list : lists) {
            if (list != null && !list.isEmpty()) {
                if (resultSet == null) {
                    resultSet = new HashSet<>(list);
                } else {
                    resultSet.retainAll(list);
                }
            }
        }
        return resultSet != null ? new ArrayList<>(resultSet) : List.of();
    }

    public Details getCocktailDetails(String id) {
        Details cocktail = new Details();
        String instructions = null;
        String thumbnail = null;
        String idDrink = null;
        String name = null;
        List<String> ingredientsList = new ArrayList<>();
        String url = UriComponentsBuilder.fromUriString(API_URL)
                .path("/lookup.php")
                .queryParam("i", id)
                .build()
                .toUriString();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                JsonReader reader = Json.createReader(new StringReader(response));
                JsonObject jsonObject = reader.readObject();

                if (jsonObject.containsKey("drinks") && !jsonObject.isNull("drinks")) {
                    JsonArray drinksArray = jsonObject.getJsonArray("drinks");
                    for (JsonObject drink : drinksArray.getValuesAs(JsonObject.class)) {
                        for (int j = 1; j <= 15; j++) { // 15 is the maximum number of ingredients/measures
                            String ingredientKey = "strIngredient" + j;
                            String measureKey = "strMeasure" + j;

                            String ingredient = drink.getString(ingredientKey, null);
                            String measure = drink.getString(measureKey, null);

                            if (ingredient != null && !ingredient.isEmpty()) {
                                // Add ingredient and measure to the list
                                if (measure != null && !measure.isEmpty()) {
                                    ingredientsList.add(measure.trim() + " " + ingredient.trim());
                                } else {
                                    ingredientsList.add(ingredient.trim()); // Add ingredient without measure
                                }
                            }
                        }

                        idDrink = drink.getString("idDrink");
                        instructions = drink.getString("strInstructions", null);
                        thumbnail = drink.getString("strDrinkThumb", null);
                        name = drink.getString("strDrink", null);
                    }

                    cocktail.setName(name);
                    cocktail.setId(idDrink);
                    cocktail.setInstructions(instructions);
                    cocktail.setIngredients(ingredientsList);
                    cocktail.setThumbnail(thumbnail);
                    return cocktail;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void saveDrinkDetails(Details details, String email) {
        partyRepo.saveDrinkDetails(details, email);
    }

    public List<Details> getFavourites(String email) {
        List<Details> favourites = partyRepo.getFavourites(email);
        return favourites != null ? favourites : List.of();
    }

    public void deleteFavourite(String email, String drinkId) {
        partyRepo.deleteFavourite(email, drinkId);
    }

    public List<Guest> getGuestList(String email) {
        List<Object> rawGuests = partyRepo.getGuestList(email);
        return rawGuests.stream()
                .map(data -> {
                    String json = (String) data;
                    JsonObject obj = Json.createReader(new StringReader(json)).readObject();
                    Guest guest = new Guest();
                    guest.setId(obj.getString("id"));
                    guest.setName(obj.getString("name"));
                    guest.setRsvp(obj.getString("rsvp"));
                    // Fix preferences processing
                    guest.setPreferences(obj.getJsonArray("preferences").stream()
                            .map(pref -> ((JsonString) pref).getString()) // Get plain string
                            .collect(Collectors.toList()));
                    return guest;
                })
                .toList();
    }

    public void addGuest(String email, Guest guest) {
        partyRepo.addGuest(email, guest);
    }

    public void deleteGuest(String email, String guestId) {
        partyRepo.deleteGuest(email, guestId);
    }

    public List<Party> getParties(String email){
        List<Object> rawParties = partyRepo.getParties(email);
        return rawParties.stream()
                .map(data -> {
                    String json = (String) data;
                    JsonObject obj = Json.createReader(new StringReader(json)).readObject();
                    Party party = new Party();
                    party.setId(obj.getString("id"));
                    party.setName(obj.getString("name"));
                    party.setDate(obj.getString("date"));
                    party.setTime(obj.getString("time"));
                    party.setLocation(obj.getString("location"));
                    party.setUserEmail(obj.getString("userEmail"));
                    return party;
                })
                .toList();
    }

    public void deleteParty(String email, String id) {
        partyRepo.deleteParty(email, id);
    }

    public Party getPartyById(String email, String id) {
        String partyJson = partyRepo.getPartyById(email, id);
        if(partyJson == null) {
            return null;
        }
        JsonObject obj = Json.createReader(new StringReader(partyJson)).readObject();
        Party party = new Party();
        party.setId(obj.getString("id"));
        party.setName(obj.getString("name"));
        party.setDate(obj.getString("date"));
        party.setTime(obj.getString("time"));
        party.setLocation(obj.getString("location"));
        party.setUserEmail(obj.getString("userEmail"));
        return party;
    }

    public void saveParty(Party updatedParty) {
        partyRepo.saveParty(updatedParty);
    }

    public List<Guest> getGuestsForParty(String id) {
        return partyRepo.getGuestsForParty(id);
    }

    public List<Details> getDrinksForParty(String id) {
        return partyRepo.getDrinksForParty(id);
    }

    public Guest getGuestById(String id, String email) {
        return partyRepo.getGuestById(id, email);
    }

    public Details getDrinkDetails(String id, String email) {
        return partyRepo.getDrinkDetails(id, email);
    }

    public void addGuestToParty(String partyId, Guest guest) {
        System.out.println("Adding guest: " + guest.getName() + " to party: " + partyId);
        partyRepo.addGuestToParty(partyId, guest);
    }

    public void removeGuestFromParty(String partyId, String guestId) {
        System.out.println("Fetching guests for party ID: " + partyId);
        List<Guest> existingGuests = partyRepo.getGuestsForParty(partyId);
        System.out.println("Before removal: " + existingGuests);

        if (existingGuests == null || existingGuests.isEmpty()) {
            System.err.println("No guests found for party ID: " + partyId);
            return;
        }

        // Remove guest by ID
        List<Guest> updatedGuests = new ArrayList<>(existingGuests);
        updatedGuests.removeIf(guest -> guest.getId().equals(guestId));

        System.out.println("After removal: " + updatedGuests);

        // Save updated list back to Redis
        partyRepo.saveGuestsToParty(partyId, updatedGuests);
        System.out.println("Guest with ID: " + guestId + " removed from party: " + partyId);
    }



    public List<Guest> getAvailableGuests(String partyId) {
        // Fetch all guests saved by the user
        List<Guest> allGuests = partyRepo.getGuestListForUser(partyId);

        // Fetch guests already added to the party
        List<Guest> guestsForParty = partyRepo.getGuestsForParty(partyId);

        // Filter out guests already assigned to the party
        return allGuests.stream()
                .filter(guest -> !guestsForParty.contains(guest))
                .toList();
    }

    public void addDrinkToParty(String partyId, Details drink) {
        System.out.println("Adding drink: " + drink.getName() + " to party: " + partyId);
        partyRepo.addDrinkToParty(partyId, drink);
    }

    public void removeDrinkFromParty(String partyId, String drinkId) {
        System.out.println("Removing drink with ID: " + drinkId + " from party: " + partyId);
        partyRepo.removeDrinkFromParty(partyId, drinkId);
    }

    public List<Details> getAvailableDrinks(String userEmail, String partyId) {
        System.out.println("Fetching bookmarked drinks for user: " + userEmail);
        List<Details> allDrinks = getFavourites(userEmail);

        System.out.println("Bookmarked drinks: " + allDrinks);

        System.out.println("Fetching drinks for party ID: " + partyId);
        List<Details> partyDrinks = getDrinksForParty(partyId);

        System.out.println("Drinks for party: " + partyDrinks);

        return allDrinks.stream()
                .filter(drink -> partyDrinks.stream().noneMatch(d -> d.getId().equals(drink.getId())))
                .toList();
    }


}
