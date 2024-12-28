package vttp.ssf.Miniproject.repositories;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import vttp.ssf.Miniproject.models.Guest;
import vttp.ssf.Miniproject.models.UserRegistration;

import java.io.StringReader;

@Repository
public class UserRepository {

    @Autowired
    @Qualifier("redisStringTemplate")
    private RedisTemplate<String, String> redisTemplate;

    private static final String EMAILS_KEY = "Emails";
    private static final String USERNAMES_KEY = "Usernames";

    public String authenticate(String identifier, String password) {

        try {
            if (identifier.contains("@")) {
                String userInfo = (String) redisTemplate.opsForHash().get(EMAILS_KEY, identifier);
                if(userInfo == null) {
                    return "Incorrect email address";
                }
                JsonObject temp = Json.createReader(new StringReader(userInfo)).readObject();
                String storedPassword = temp.getString("password");
                if (!password.equals(storedPassword)) {
                    return "Incorrect password";
                }
                return "Success";
            }

            String userInfo = (String) redisTemplate.opsForHash().get(USERNAMES_KEY, identifier);
            if(userInfo == null) {
                return "Incorrect username";
            }
            JsonObject temp = Json.createReader(new StringReader(userInfo)).readObject();
            String storedPassword = temp.getString("password");
            if (!password.equals(storedPassword)) {
                return "Incorrect password";
            }
            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "Username/ Email Address not found";
        }
    }

    public void saveNewUser(UserRegistration user) {
        System.out.println("Saving new user: " + user);
        String userInfo = Json.createObjectBuilder()
                .add("username", user.getUsername())
                .add("email", user.getEmail())
                .add("password", user.getPassword())
                .build()
                .toString();
        redisTemplate.opsForHash().put(EMAILS_KEY, user.getEmail(), userInfo);
        System.out.println("Email/ Password saved");
        redisTemplate.opsForHash().put(USERNAMES_KEY, user.getUsername(), userInfo);
        System.out.println("Username/ Password saved");
    }

    public String findUsername(String identifier) {
        if (identifier.contains("@")) {
            String userInfo = (String) redisTemplate.opsForHash().get(EMAILS_KEY, identifier);
            if(userInfo == null) {
                return null;
            }
            JsonObject temp = Json.createReader(new StringReader(userInfo)).readObject();
            return temp.getString("username");
        }
        String userInfo = (String) redisTemplate.opsForHash().get(USERNAMES_KEY, identifier);
        if(userInfo == null) {
            return null;
        }
        JsonObject temp = Json.createReader(new StringReader(userInfo)).readObject();
        return temp.getString("username");
    }

    public String findEmail(String identifier) {
        String userIndoor = (String) redisTemplate.opsForHash().get(USERNAMES_KEY, identifier);
        if(userIndoor == null) {
            return null;
        }
        JsonObject temp = Json.createReader(new StringReader(userIndoor)).readObject();
        return temp.getString("email");
    }

    public boolean checkUserExists(String identifier) {
        if (identifier.contains("@")) {
            Boolean emailExists = redisTemplate.opsForHash().hasKey(EMAILS_KEY,identifier);
            if (!emailExists) {
                return false;
            }
            return true;
        }
        Boolean usernameExists = redisTemplate.opsForHash().hasKey(USERNAMES_KEY,identifier);
        if (!usernameExists) {
            return false;
        }
        return true;
    }

    public String getRandomKey() throws Exception{
        return (String) redisTemplate.randomKey();
    }

}
