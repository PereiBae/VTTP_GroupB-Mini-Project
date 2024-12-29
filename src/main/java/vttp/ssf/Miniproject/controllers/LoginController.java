package vttp.ssf.Miniproject.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vttp.ssf.Miniproject.models.UserLogin;
import vttp.ssf.Miniproject.models.UserRegistration;
import vttp.ssf.Miniproject.services.LoginService;
import vttp.ssf.Miniproject.utils.AgeCalculator;

import java.util.Date;

@Controller
@RequestMapping
public class LoginController {

    @Autowired
    private LoginService loginSvc;

    // Redirects to the home page.
    @GetMapping
    public String getHomePage() {
        return "redirect:/home";
    }

    // Returns the login page view.
    @GetMapping("/login")
    public String getLogin(Model model, HttpSession session) {
        if(session.getAttribute("user") == null){
            UserLogin user = new UserLogin();
            model.addAttribute("userlogin", user);
            return "login";
        }
            return "redirect:/home";
    }

    // Processes login information and starts a session if successful.
    @PostMapping("/loggingin")
    public String postLogin(@Valid @ModelAttribute("userlogin") UserLogin user, BindingResult bindingResult, Model model, HttpSession session) {

        // Validation error
        if (bindingResult.hasErrors()) {
            System.out.println("Validation errors occurred. " + bindingResult.getAllErrors() + "\n");
            model.addAttribute("userlogin", user);
            return "login";
        }

        // Convert identifier to be a username/password
        // Authenticate the login information
        if (!loginSvc.authenticate(user.getIdentifier(), user.getPassword())){
            System.out.println("User is not authenticated\n");
            model.addAttribute("message", "Invalid Username/Email or Password");
            return "login";
        }

        // Ensure a session is made
        String username = loginSvc.findUsername(user.getIdentifier());
        String userEmail = null;
        if (user.getIdentifier().contains("@")){
            userEmail = user.getIdentifier();
        } else{
            userEmail = loginSvc.findEmail(user.getIdentifier());
        }
        session.setAttribute("user", username);
        session.setAttribute("userEmail",userEmail);

        return "redirect:/home";
    }

    // Returns the registration page view.
    @GetMapping("/register")
    public String getRegister(Model model, HttpSession session) {
        if(session.getAttribute("user") != null){
            return "redirect:/home";
        }
        UserRegistration user = new UserRegistration();
        model.addAttribute("userregister", user);
        return "register";
    }

    // Processes user registration.
    @PostMapping("/registration")
    public String postRegistration(@Valid @ModelAttribute("userregister") UserRegistration user, BindingResult bindingResult, Model model, HttpSession session) {

        // Validation of fields
        if (bindingResult.hasErrors()) {
            System.out.println("Validation errors occurred. " + bindingResult.getAllErrors() +"\n");
            model.addAttribute("userregister", user);
            return "register";
        }

        if(loginSvc.userExists(user.getUsername())){
            model.addAttribute("message", "Username is already taken");
            model.addAttribute("userregister", user);
            return "register";
        }

        if(loginSvc.userExists(user.getEmail())){
            model.addAttribute("message", "Email is already in use");
            model.addAttribute("userregister", user);
            return "register";
        }

        Date birthDate = user.getBirthDate();

        if (AgeCalculator.calculateAge(birthDate) < 18) {
            System.out.println("User is too young\n");
            model.addAttribute("userregister", user);
            model.addAttribute("message", "You are too young to access this resource");
            return "tooYoung";
        }
        System.out.println("User is old enough\n");

        loginSvc.saveNewUser(user);
        session.setAttribute("user", user.getUsername());
        session.setAttribute("userEmail",user.getEmail());
        return "redirect:/home";
    }

    // Invalidates the session and logs the user out.
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear the session
        return "redirect:/home";
    }

    // A health check endpoint.
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<HttpStatus> getStatus() {
        if (loginSvc.getRandomKey()) {
            return new ResponseEntity<>(HttpStatus.valueOf(503));
        }
        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }
}
