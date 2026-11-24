package devops.projet_devops;

import tools.jackson.databind.ObjectMapper;
import devops.projet_devops.controller.UserController;
import devops.projet_devops.model.User;
import devops.projet_devops.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User("Jean", "Dupont", "jean.dupont@example.com", 25);
        validUser.setId(1L);
    }

    @Nested
    @DisplayName("GET /api/users")
    class FindAll {

        @Test
        @DisplayName("retourne la liste de tous les utilisateurs")
        void shouldReturnAllUsers() throws Exception {
            when(userService.findAll()).thenReturn(List.of(validUser));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].firstName").value("Jean"))
                    .andExpect(jsonPath("$[0].lastName").value("Dupont"))
                    .andExpect(jsonPath("$[0].email").value("jean.dupont@example.com"))
                    .andExpect(jsonPath("$[0].age").value(25));

            verify(userService, times(1)).findAll();
        }

        @Test
        @DisplayName("retourne une liste vide quand aucun utilisateur n'existe")
        void shouldReturnEmptyList() throws Exception {
            when(userService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class FindById {

        @Test
        @DisplayName("retourne 200 et l'utilisateur quand il existe")
        void shouldReturnUserWhenFound() throws Exception {
            when(userService.findById(1L)).thenReturn(Optional.of(validUser));

            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Jean"));
        }

        @Test
        @DisplayName("retourne 404 quand l'utilisateur n'existe pas")
        void shouldReturnNotFoundWhenMissing() throws Exception {
            when(userService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class Create {

        @Test
        @DisplayName("retourne 201 et l'utilisateur créé")
        void shouldCreateUser() throws Exception {
            User newUser = new User("Marie", "Curie", "marie.curie@example.com", 30);
            User savedUser = new User("Marie", "Curie", "marie.curie@example.com", 30);
            savedUser.setId(2L);

            when(userService.save(any(User.class))).thenReturn(savedUser);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newUser)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.firstName").value("Marie"))
                    .andExpect(jsonPath("$.email").value("marie.curie@example.com"));

            verify(userService, times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class Update {

        @Test
        @DisplayName("retourne 200 et l'utilisateur mis à jour")
        void shouldUpdateUser() throws Exception {
            User updatedUser = new User("Jean", "Martin", "jean.martin@example.com", 26);
            updatedUser.setId(1L);

            when(userService.findById(1L)).thenReturn(Optional.of(validUser));
            when(userService.save(any(User.class))).thenReturn(updatedUser);

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lastName").value("Martin"))
                    .andExpect(jsonPath("$.email").value("jean.martin@example.com"));
        }

        @Test
        @DisplayName("retourne 404 quand l'utilisateur n'existe pas")
        void shouldReturnNotFoundWhenMissing() throws Exception {
            when(userService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/users/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUser)))
                    .andExpect(status().isNotFound());

            verify(userService, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class Delete {

        @Test
        @DisplayName("retourne 204 quand l'utilisateur est supprimé")
        void shouldDeleteUser() throws Exception {
            when(userService.findById(1L)).thenReturn(Optional.of(validUser));

            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("retourne 404 quand l'utilisateur n'existe pas")
        void shouldReturnNotFoundWhenMissing() throws Exception {
            when(userService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/users/99"))
                    .andExpect(status().isNotFound());

            verify(userService, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("GET /api/users/is-adult")
    class IsAdult {

        @Test
        @DisplayName("retourne true pour un adulte")
        void shouldReturnTrueForAdult() throws Exception {
            when(userService.isAdult(25)).thenReturn(true);

            mockMvc.perform(get("/api/users/is-adult").param("age", "25"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("retourne false pour un mineur")
        void shouldReturnFalseForMinor() throws Exception {
            when(userService.isAdult(15)).thenReturn(false);

            mockMvc.perform(get("/api/users/is-adult").param("age", "15"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }

    @Nested
    @DisplayName("GET /api/users/validate-email")
    class ValidateEmail {

        @Test
        @DisplayName("retourne true pour un email valide")
        void shouldReturnTrueForValidEmail() throws Exception {
            when(userService.isValidEmail("user@example.com")).thenReturn(true);

            mockMvc.perform(get("/api/users/validate-email").param("email", "user@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("retourne false pour un email invalide")
        void shouldReturnFalseForInvalidEmail() throws Exception {
            when(userService.isValidEmail("invalid")).thenReturn(false);

            mockMvc.perform(get("/api/users/validate-email").param("email", "invalid"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }

    @Nested
    @DisplayName("GET /api/users/username")
    class GenerateUsername {

        @Test
        @DisplayName("retourne le username généré")
        void shouldReturnGeneratedUsername() throws Exception {
            when(userService.generateUsername("Jean", "Dupont")).thenReturn("jean.dupont");

            mockMvc.perform(get("/api/users/username")
                            .param("firstName", "Jean")
                            .param("lastName", "Dupont"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("jean.dupont"));
        }
    }
}