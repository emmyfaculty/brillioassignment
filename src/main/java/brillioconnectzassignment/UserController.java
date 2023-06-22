package brillioconnectzassignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

        @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    public String validateUser(@RequestBody User user) {
        String token = userService.validateUser(user);
        if (token != null) {
            return token;
        } else {
            // Return an error message or handle the validation failure as per your requirements
            return "User validation failed.";
        }
    }

    @PostMapping("/verifyToken")
    public String verifyToken(@RequestBody String token) {
        String verificationResult = userService.verifyToken(token);
        if (verificationResult.equals("Verification pass")) {
            // Token is valid
            return "User verification passed.";
        } else {
            // Token is invalid
            return "User verification failed.";
        }
    }
}

