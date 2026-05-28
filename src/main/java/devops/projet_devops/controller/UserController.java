package devops.projet_devops.controller;

import devops.projet_devops.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/is-adult")
    public boolean isAdult(@RequestParam int age) {
        return userService.isAdult(age);
    }

    @GetMapping("/validate-email")
    public boolean validateEmail(@RequestParam String email) {
        return userService.isValidEmail(email);
    }

    @GetMapping("/username")
    public String generateUsername(
            @RequestParam String firstName,
            @RequestParam String lastName
    ) {
        return userService.generateUsername(firstName, lastName);
    }
}
