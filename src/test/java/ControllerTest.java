import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.controllers.CardController;
import org.example.app.controllers.SessionController;
import org.example.app.controllers.UserController;
import org.example.app.models.PasswordUtils;
import org.example.app.models.User;
import org.example.app.repositories.CardRepository;
import org.example.app.repositories.UserRepository;
import org.example.app.services.CardService;
import org.example.app.services.TradingService;
import org.example.app.services.UserService;
import org.example.server.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ControllerTest {
    User authenticatedUser = new User("kienboec", "test");
    UserRepository userRepositorySpy;
    CardRepository cardRepositorySpy;
    CardService cardService = new CardService();
    TradingService tradingService = new TradingService();
    UserService userService = new UserService();

    public ControllerTest() throws NoSuchAlgorithmException {
    }

    @BeforeAll
    void setupController() {
        userRepositorySpy
                = Mockito.spy(Mockito.mock(UserRepository.class));
        Mockito.doNothing().when(userRepositorySpy).insert(Mockito.any());
        Mockito.doNothing().when(userRepositorySpy).update(Mockito.any());

        cardRepositorySpy
                = Mockito.spy(Mockito.mock(CardRepository.class));
        Mockito.doNothing().when(cardRepositorySpy).insert(Mockito.any());
        Mockito.doNothing().when(cardRepositorySpy).update(Mockito.any(), Mockito.any());
    }

    @Test
    void testUserController() throws NoSuchAlgorithmException, JsonProcessingException {
        UserController userController = new UserController(userService, userRepositorySpy);

        Response create = userController.createUser("{\"Username\":\"kienboec\", \"Password\":\"daniel\"}");
        assertEquals(create.getStatusCode(), 201);

        Response put = userController.putUser("{\"Name\": \"Kienboeck\",  \"Bio\": \"me playin...\", \"Image\": \":-)\"}", "kienboec", authenticatedUser);
        assertEquals(put.getStatusCode(), 200);

        Response get = userController.getUser("kienboec", authenticatedUser);
        assertEquals(get.getStatusCode(), 200);
        User kienbocUser = userService.getUser("kienboec");

        assertEquals(kienbocUser.getUsername(), "kienboec");
        assertEquals(kienbocUser.getName(), "Kienboeck");
        assertEquals(kienbocUser.getBio(), "me playin...");
        assertEquals(kienbocUser.getImage(), ":-)");
        assertEquals(kienbocUser.getCoins(), 20);
        assertEquals(kienbocUser.getPasswordHash(), PasswordUtils.hashPassword("daniel".toCharArray()));
    }

    @Test
    void testSessionController() throws JsonProcessingException {
        SessionController sessionController = new SessionController(userService);

        Response login = sessionController.login("{\"Username\":\"kienboec\", \"Password\":\"WRONG\"}");
        assertEquals(login.getStatusCode(), 401);

        login = sessionController.login("{\"Username\":\"kienboec\", \"Password\":\"daniel\"}");
        assertEquals(login.getStatusCode(), 200);

        User authenticatedUser1 = sessionController.getAuthenticatedUser("kienboec-mtcgToken");
        assertEquals(authenticatedUser1.getUsername(), "kienboec");
    }


}
