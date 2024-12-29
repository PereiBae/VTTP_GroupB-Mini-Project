package vttp.ssf.Miniproject.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vttp.ssf.Miniproject.models.Cocktail;
import vttp.ssf.Miniproject.models.Details;
import vttp.ssf.Miniproject.models.Guest;
import vttp.ssf.Miniproject.models.Party;
import vttp.ssf.Miniproject.services.LoginService;
import vttp.ssf.Miniproject.services.PartyService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/home")
public class PartyController {

    @Autowired
    private PartyService partyService;

    @Autowired
    private LoginService loginService;

    // Displays the home page with a welcome message.
    @GetMapping
    public String getHomePage(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            model.addAttribute("message", "Welcome Guest!");
        }
        String username = (String) session.getAttribute("user");
        model.addAttribute("username", username);
        model.addAttribute("message", "Welcome " + username + "!");
        return "home";
    }

    // Displays paginated search results for cocktails.
    @GetMapping("/results")
    public String getPaginatedResults(
            @RequestParam(required = false) String ingredient,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String alcoholic,
            @RequestParam(required = false, defaultValue = "1") int page,
            Model model) {

        int pageSize = 12;

        // Fetch dropdown data for filters
        List<String> ingredients = partyService.getIngredients();
        List<String> categories = partyService.getCategories();
        List<String> alcoholicFilters = partyService.getAlcoholicFilters();

        // Add dropdown data to the model
        model.addAttribute("ingredients", ingredients);
        model.addAttribute("categories", categories);
        model.addAttribute("alcoholicFilters", alcoholicFilters);

        // Add current filter selections to the model
        model.addAttribute("currentIngredient", ingredient);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentAlcoholic", alcoholic);

        // Fetch results
        List<Cocktail> allResults = partyService.searchCocktails(ingredient, category, alcoholic);

        if (allResults.isEmpty()) {
            model.addAttribute("results", List.of());
            model.addAttribute("currentPage", 1);
            model.addAttribute("totalPages", 1);
            model.addAttribute("message", "No results found. Please try different filters.");
            return "results";
        }

        List<Cocktail> uniqueResults = allResults.stream().distinct().toList();
        int totalResults = uniqueResults.size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);

        if (page < 1) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, totalResults);

        List<Cocktail> paginatedResults = uniqueResults.subList(start, end);

        model.addAttribute("results", paginatedResults);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "results";
    }

    // Fetches detailed information about a specific cocktail.
    @GetMapping("/details/{drinkId}")
    public String getDetailPage(@PathVariable String drinkId, Model model) {
        Details details = partyService.getCocktailDetails(drinkId);
        model.addAttribute("details", details);
        return "drinkDetails";
    }

    // Saves a cocktail to the user's favorites.
    @PostMapping("/details/{drinkId}")
    public String postDetailPage(@PathVariable String drinkId, HttpSession session) {
        if (session.getAttribute("user") != null) {
           String email = (String) session.getAttribute("userEmail");
           Details details = partyService.getCocktailDetails(drinkId);
           partyService.saveDrinkDetails(details, email);
           return "redirect:/home/details/" + drinkId;
        }
        return "redirect:/login";
    }

    // Displays the user's favorite cocktails.
    @GetMapping("/favourites")
    public String getFavourites(Model model, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");

        if (userEmail == null) {
            return "redirect:/login"; // Redirect to login if no user is logged in
        }

        List<Details> favourites = partyService.getFavourites(userEmail);
        model.addAttribute("favourites", favourites);
        return "favourites";
    }

    // Removes a drink from the favourites
    @PostMapping("/favourites/delete/{id}")
    public String deleteFavourite(@PathVariable String id, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email != null) {
            partyService.deleteFavourite(email, id);
        }
        return "redirect:/home/favourites";
    }

    // Displays the user's guests and Add a guest
    @GetMapping("/guests")
    public String getGuests(Model model, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");

        if (userEmail == null) {
            return "redirect:/login"; // Redirect to login if no user is logged in
        }

        List<Guest> guests = partyService.getGuestList(userEmail);
        model.addAttribute("guests", guests);
        model.addAttribute("guest", new Guest());
        return "guests";

    }

    // Adds a guest to the list of guests for that user
    @PostMapping("/guests/add")
    public String addGuest(@Valid @ModelAttribute("guest") Guest guest, BindingResult bindingResult, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "guests";
        }

        if (guest.getId() == null) {
            String guestId = UUID.randomUUID().toString().substring(0, 7);
            guest.setId(guestId);
        }
        partyService.addGuest(userEmail, guest);
        return "redirect:/home/guests";
    }

    // Removes a guest from the list of guests of that user
    @PostMapping("/guests/delete/{id}")
    public String deleteGuest(@PathVariable String id, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }
        partyService.deleteGuest(userEmail, id);
        return "redirect:/home/guests";
    }

    // Displays all parties for the user.
    @GetMapping("/parties")
    public String viewParties(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        List<Party> partyList = partyService.getParties(userEmail);
        model.addAttribute("parties", partyList);
        return "parties";
    }

    // Delete parties for the user
    @PostMapping("/parties/delete/{id}")
    public String deleteParty(@PathVariable String id, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        partyService.deleteParty(userEmail, id);
        return "redirect:/home/parties";
    }

    // Displays a page for the user th=o edit their parties
    @GetMapping("/parties/edit/{id}")
    public String editPartyForm(@PathVariable String id, HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        Party party = partyService.getPartyById(userEmail, id);
        if (party == null) {
            return "redirect:/home/parties";
        }

        model.addAttribute("party", party);
        return "partyEdit";
    }

    // Displays a page with an updated list of parties for the user
    @PostMapping("/parties/edit/{id}")
    public String editedParty(@ModelAttribute Party party ,@PathVariable String id, HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        party.setUserEmail(userEmail);
        partyService.saveParty(party);
        return "redirect:/home/parties";
    }

    // Allows user to create a new party
    @GetMapping("/parties/create")
    public String showPartyCreationForm(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        model.addAttribute("party", new Party()); // Add a blank party object for the form
        return "partyCreate";
    }

    // Creates the party and updates the list of parties for the user
    @PostMapping("/parties/create")
    public String createParty(@Valid @ModelAttribute("party") Party party, BindingResult bindingResult, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "partyCreate";
        }

        if (party.getId() == null) {
            String partyId = UUID.randomUUID().toString().substring(0, 6);
            party.setId(partyId);
        }

        party.setUserEmail(userEmail);
        partyService.saveParty(party); // Save party details
        return "redirect:/home/parties"; // Redirect to the party management page
    }

    // Displays details of a specific party, including guests and drinks
    @GetMapping("/parties/details/{id}")
    public String viewPartyDetails(@PathVariable String id, HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        // Fetch party details
        Party party = partyService.getPartyById(userEmail, id);
        if (party == null) {
            return "redirect:/home/parties";
        }

        // Fetch guests and drinks associated with the party
        List<Guest> guestsForParty = partyService.getGuestsForParty(id);
        List<Details> drinks = partyService.getDrinksForParty(id);

        // Fetch all guests for the user and filter out those already in the party
        List<Guest> availableGuests = partyService.getAvailableGuests(userEmail);
        List<Details> availableDrinks = partyService.getAvailableDrinks(userEmail, id);

        // Add data to the model
        model.addAttribute("party", party);
        model.addAttribute("guests", guestsForParty);
        model.addAttribute("drinks", drinks);
        model.addAttribute("availableGuests", availableGuests); // For the dropdown
        model.addAttribute("availableDrinks", availableDrinks); // For the dropdown

        return "testPartyDetails";
    }

    // Adds guests to specific parties
    @PostMapping("/party/{id}/addGuest")
    public String addGuestToParty(@PathVariable String id, @RequestParam String guestId, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }
        System.out.println("Guest ID received: " + guestId);
        Guest guest = partyService.getGuestById(guestId, userEmail);
        if (guest != null) {
            partyService.addGuestToParty(id, guest);
            System.out.println("Guest added to party: " + guest.getName());
        } else {
            System.err.println("Guest not found for ID: " + guestId);
        }
        return "redirect:/home/parties/details/" + id;
    }

    // Removes guests from specific parties
    @PostMapping("/party/{id}/removeGuest/{guestId}")
    public String removeGuestFromParty(@PathVariable String id, @PathVariable String guestId, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        System.out.println("Removing guest with ID: " + guestId + " from party ID: " + id);
        partyService.removeGuestFromParty(id, guestId);

        return "redirect:/home/parties/details/" + id;
    }

    // Adds drinks from favourite drinks to the specific party
    @PostMapping("/party/{id}/addDrink")
    public String addDrinkToParty(@PathVariable String id, @RequestParam String drinkId, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/login";
        }

        Details drink = partyService.getDrinkDetails(drinkId, userEmail);
        if (drink == null) {
            System.err.println("Drink with ID " + drinkId + " not found.");
            return "redirect:/home/parties/details/" + id;
        }

        partyService.addDrinkToParty(id, drink);
        return "redirect:/home/parties/details/" + id;
    }

    // Remove drinks from the specific parties
    @PostMapping("/party/{id}/removeDrink/{drinkId}")
    public String removeDrinkFromParty(@PathVariable String id, @PathVariable String drinkId) {
        partyService.removeDrinkFromParty(id, drinkId);
        return "redirect:/home/parties/details/" + id;
    }


}
