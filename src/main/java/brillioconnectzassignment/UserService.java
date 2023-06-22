package brillioconnectzassignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String validateUser(User user) {
        List<CompletableFuture<String>> validationFutures = new ArrayList<>();

        // Validate username
        CompletableFuture<String> usernameValidation = CompletableFuture.supplyAsync(() -> {
            if (user.getUsername() == null || user.getUsername().length() < 4) {
                return "Username: must be at least 4 characters long.";
            }
            return null;
        });
        validationFutures.add(usernameValidation);

        // Validate email
        CompletableFuture<String> emailValidation = CompletableFuture.supplyAsync(() -> {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return "Email: cannot be empty.";
            }
            // Perform email format validation using regular expression
            if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return "Email: must be a valid email address.";
            }
            return null;
        });
        validationFutures.add(emailValidation);

        // Validate password
        CompletableFuture<String> passwordValidation = CompletableFuture.supplyAsync(() -> {
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return "Password: cannot be empty.";
            }
            // Perform password strength validation using regular expression
            if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
                return "Password: must be a strong password.";
            }
            return null;
        });
        validationFutures.add(passwordValidation);

        // Validate date of birth
        CompletableFuture<String> dobValidation = CompletableFuture.supplyAsync(() -> {
            if (user.getDateOfBirth() == null) {
                return "Date of Birth: cannot be empty.";
            }
            LocalDate now = LocalDate.now();
            LocalDate minDob = now.minusYears(16);
            if (user.getDateOfBirth().isAfter(minDob)) {
                return "Date of Birth: should be 16 years or greater.";
            }
            return null;
        });
        validationFutures.add(dobValidation);

        try {
            // Wait for all validations to complete
            CompletableFuture.allOf(validationFutures.toArray(new CompletableFuture[0])).get();

            // Collect the validation results
            List<String> validationErrors = new ArrayList<>();
            for (CompletableFuture<String> validationFuture : validationFutures) {
                String validationError = validationFuture.get();
                if (validationError != null) {
                    validationErrors.add(validationError);
                }
            }

            if (validationErrors.isEmpty()) {
                // Save the user to the database
                userRepository.save(user);

                // Generate JWT if all validations pass
                String token = JWTUtil.generateToken(user);
                System.out.println("JWT Token: " + token);
                return token;
            } else {
                // Return validation errors
                for (String validationError : validationErrors) {
                    System.out.println(validationError);
                }
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String verifyToken(String token) {
        if (JWTUtil.validateToken(token)) {
            return "Verification pass";
        } else {
            return "Verification fails";
        }
    }
}


