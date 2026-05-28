package devops.projet_devops.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public boolean isAdult(int age) {
        return age >= 18;
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.contains("@") && email.contains(".");
    }

    public String generateUsername(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("firstName and lastName cannot be null");
        }
        String normalizedFirstName = firstName.trim().toLowerCase();
        String normalizedLastName = lastName.trim().toLowerCase();
        return normalizedFirstName + "." + normalizedLastName;
    }
}
