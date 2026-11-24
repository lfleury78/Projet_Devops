package devops.projet_devops.service;

import java.util.List;
import java.util.Optional;

import devops.projet_devops.model.User;
import devops.projet_devops.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User save(User user) {
        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email");
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

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
