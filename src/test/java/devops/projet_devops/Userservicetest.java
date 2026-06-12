package devops.projet_devops;

import devops.projet_devops.model.User;
import devops.projet_devops.service.UserService;
import devops.projet_devops.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class  UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User("Jean", "Dupont", "jean.dupont@example.com", 25);
        validUser.setId(1L);
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("retourne la liste de tous les utilisateurs")
        void shouldReturnAllUsers() {
            when(userRepository.findAll()).thenReturn(List.of(validUser));

            List<User> result = userService.findAll();

            assertThat(result).hasSize(1).contains(validUser);
            verify(userRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("retourne une liste vide quand aucun utilisateur n'existe")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            assertThat(userService.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("retourne l'utilisateur quand il existe")
        void shouldReturnUserWhenFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));

            Optional<User> result = userService.findById(1L);

            assertThat(result).isPresent().contains(validUser);
        }

        @Test
        @DisplayName("retourne Optional.empty() quand l'utilisateur n'existe pas")
        void shouldReturnEmptyWhenNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(userService.findById(99L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("sauvegarde et retourne l'utilisateur avec un email valide")
        void shouldSaveUserWithValidEmail() {
            when(userRepository.save(validUser)).thenReturn(validUser);

            User saved = userService.save(validUser);

            assertThat(saved).isEqualTo(validUser);
            verify(userRepository, times(1)).save(validUser);
        }

        @Test
        @DisplayName("lève une exception avec un email sans @")
        void shouldThrowWhenEmailMissingAt() {
            validUser.setEmail("invalide.com");

            assertThatThrownBy(() -> userService.save(validUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("lève une exception avec un email null")
        void shouldThrowWhenEmailNull() {
            validUser.setEmail(null);

            assertThatThrownBy(() -> userService.save(validUser))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("lève une exception avec un email vide")
        void shouldThrowWhenEmailBlank() {
            validUser.setEmail("   ");

            assertThatThrownBy(() -> userService.save(validUser))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("appelle deleteById sur le repository")
        void shouldCallRepositoryDelete() {
            userService.deleteById(1L);

            verify(userRepository, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("isAdult()")
    class IsAdult {

        @Test
        @DisplayName("retourne true pour un âge >= 18")
        void shouldReturnTrueWhenAdult() {
            assertThat(userService.isAdult(18)).isTrue();
            assertThat(userService.isAdult(30)).isTrue();
        }

        @Test
        @DisplayName("retourne false pour un âge < 18")
        void shouldReturnFalseWhenMinor() {
            assertThat(userService.isAdult(17)).isFalse();
            assertThat(userService.isAdult(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isValidEmail()")
    class IsValidEmail {

        @Test
        @DisplayName("retourne true pour un email valide")
        void shouldReturnTrueForValidEmail() {
            assertThat(userService.isValidEmail("user@example.com")).isTrue();
        }

        @Test
        @DisplayName("retourne false pour un email sans @")
        void shouldReturnFalseWhenMissingAt() {
            assertThat(userService.isValidEmail("userexample.com")).isFalse();
        }

        @Test
        @DisplayName("retourne false pour un email sans point")
        void shouldReturnFalseWhenMissingDot() {
            assertThat(userService.isValidEmail("user@examplecom")).isFalse();
        }

        @Test
        @DisplayName("retourne false pour un email null")
        void shouldReturnFalseForNull() {
            assertThat(userService.isValidEmail(null)).isFalse();
        }

        @Test
        @DisplayName("retourne false pour un email vide")
        void shouldReturnFalseForBlank() {
            assertThat(userService.isValidEmail("")).isFalse();
        }
    }

    @Nested
    @DisplayName("generateUsername()")
    class GenerateUsername {

        @Test
        @DisplayName("génère le username au format prénom.nom en minuscules")
        void shouldGenerateCorrectUsername() {
            assertThat(userService.generateUsername("Jean", "Dupont"))
                    .isEqualTo("jean.dupont");
        }

        @Test
        @DisplayName("supprime les espaces et met en minuscules")
        void shouldTrimAndLowercase() {
            assertThat(userService.generateUsername("  Marie  ", "  Curie  "))
                    .isEqualTo("marie.curie");
        }

        @Test
        @DisplayName("lève une exception si le prénom est null")
        void shouldThrowWhenFirstNameNull() {
            assertThatThrownBy(() -> userService.generateUsername(null, "Dupont"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("lève une exception si le nom est null")
        void shouldThrowWhenLastNameNull() {
            assertThatThrownBy(() -> userService.generateUsername("Jean", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}