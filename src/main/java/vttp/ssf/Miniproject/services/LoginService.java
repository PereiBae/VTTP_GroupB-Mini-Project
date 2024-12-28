package vttp.ssf.Miniproject.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vttp.ssf.Miniproject.models.UserRegistration;
import vttp.ssf.Miniproject.repositories.UserRepository;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepo;

    public boolean authenticate(String identifier, String password) {

        String message = userRepo.authenticate(identifier, password);
        if (!message.equals("Success")) {
            System.out.println("Authentication failed: " + message);
            return false;
        }
        return true;
    }

    public void saveNewUser(UserRegistration user) {
        userRepo.saveNewUser(user);
        System.out.println("User has been saved to Redis");
    }

    public String findUsername(String identifier) {
        return userRepo.findUsername(identifier);
    }

    public String findEmail(String identifier) {
        return userRepo.findEmail(identifier);
    }

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
