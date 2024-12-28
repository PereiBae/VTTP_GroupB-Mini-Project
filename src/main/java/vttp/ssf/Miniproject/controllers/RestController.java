package vttp.ssf.Miniproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vttp.ssf.Miniproject.models.Details;
import vttp.ssf.Miniproject.services.PartyService;

@org.springframework.web.bind.annotation.RestController
@RequestMapping
public class RestController {

    @Autowired
    private PartyService partyService;

    @GetMapping(path = "/getDrinkDetails/{id}", produces = "text/csv")
    public ResponseEntity<byte[]> getDrinkDetailsAsCsv(@PathVariable String id) {
        Details drinkDetails = partyService.getCocktailDetails(id);

        // Generate CSV content
        String csvBuilder = "Name,Drink ID,Ingredients,Instructions,Thumbnail\n" +
                "\"" + drinkDetails.getName() + "\"," +
                "\"" + drinkDetails.getId() + "\"," +
                "\"" + String.join(" | ", drinkDetails.getIngredients()) + "\"," +
                "\"" + drinkDetails.getInstructions() + "\"," +
                "\"" + drinkDetails.getThumbnail() + "\"\n";

        // Convert CSV content to bytes
        byte[] csvBytes = csvBuilder.getBytes();

        // Set response headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=drinkDetails.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv"));

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

}
