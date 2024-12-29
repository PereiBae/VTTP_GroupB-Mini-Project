package vttp.ssf.Miniproject.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vttp.ssf.Miniproject.models.UserRegistration;
import vttp.ssf.Miniproject.repositories.UserRepository;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepo;

    // Authenticates a user by checking their email/username and password against stored data.
    public boolean authenticate(String identifier, String password) {

        String message = userRepo.authenticate(identifier, password);
        if (!message.equals("Success")) {
            System.out.println("Authentication failed: " + message);
            return false;
        }
        return true;
    }

    // Saves a new user's details (username, password, email, and other attributes) into Redis.
    public void saveNewUser(UserRegistration user) {
        userRepo.saveNewUser(user);
        System.out.println("User has been saved to Redis");
    }

    // Retrieves the username of a user using their email.
    public String findUsername(String identifier) {
        return userRepo.findUsername(identifier);
    }

    // Retrieves the email of a user using their username.
    public String findEmail(String identifier) {
        return userRepo.findEmail(identifier);
    }

    // Checks if a user exists by looking up the email or username in Redis.
    public boolean userExists(String identifier) {
        return userRepo.checkUserExists(identifier);
    }

    public boolean getRandomKey() {
        try{
            String healthCheck = userRepo.getRandomKey();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
