import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.controllers.*;
import org.example.app.models.*;
import org.example.app.models.Package;
import org.example.app.repositories.CardRepository;
import org.example.app.repositories.UserRepository;
import org.example.app.services.CardService;
import org.example.app.services.TradingService;
import org.example.app.services.UserService;
import org.example.server.Response;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerTest {
    User authenticatedUser;
    User adminUser;
    UserRepository userRepositorySpy;
    CardRepository cardRepositorySpy;
    CardService cardService = new CardService();
    TradingService tradingService = new TradingService();
    UserService userService = new UserService();

    public ControllerTest() {
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
        Mockito.doNothing().when(cardRepositorySpy).updateDeck(Mockito.any());

    }

    @Test
    @Order(1)
    void testUserAndSessionController() throws NoSuchAlgorithmException, JsonProcessingException {
        UserController userController = new UserController(userService, userRepositorySpy);
        SessionController sessionController = new SessionController(userService);

        Response create = userController.createUser("{\"Username\":\"kienboec\", \"Password\":\"daniel\"}");
        assertEquals(201, create.getStatusCode());

        Response login = sessionController.login("{\"Username\":\"kienboec\", \"Password\":\"WRONG\"}");
        assertEquals(401, login.getStatusCode());

        login = sessionController.login("{\"Username\":\"kienboec\", \"Password\":\"daniel\"}");
        assertEquals(200, login.getStatusCode());

        User authenticatedUser1 = sessionController.getAuthenticatedUser("kienboec-mtcgToken");
        assertEquals(authenticatedUser1.getUsername(), "kienboec");
        authenticatedUser = authenticatedUser1;

        Response put = userController.putUser("{\"Name\": \"Kienboeck\",  \"Bio\": \"me playin...\", \"Image\": \":-)\"}", "kienboec", authenticatedUser);
        assertEquals(200, put.getStatusCode());

        Response get = userController.getUser("kienboec", authenticatedUser);
        assertEquals(200, get.getStatusCode());
        User kienbocUser = userService.getUser("kienboec");

        assertEquals("kienboec", kienbocUser.getUsername());
        assertEquals("Kienboeck", kienbocUser.getName());
        assertEquals("me playin...", kienbocUser.getBio());
        assertEquals(":-)", kienbocUser.getImage());
        assertEquals(20, kienbocUser.getCoins());
        assertEquals(PasswordUtils.hashPassword("daniel".toCharArray()), kienbocUser.getPasswordHash());

        userController.createUser("{\"Username\":\"admin\",    \"Password\":\"istrator\"}");
        adminUser = userService.getUser("admin");
        assertFalse(authenticatedUser.isAdmin());
        assertTrue(adminUser.isAdmin());
    }

    @Test
    @Order(2)
    void testPackageController() throws JsonProcessingException {
        PackageController packageController = new PackageController(cardService, cardRepositorySpy);

        String p0 = "[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]";
        String p1 = "[{\"Id\":\"644808c2-f87a-4600-b313-122b02322fd5\", \"Name\":\"WaterGoblin\", \"Damage\":  9.0}, {\"Id\":\"4a2757d6-b1c3-47ac-b9a3-91deab093531\", \"Name\":\"Dragon\", \"Damage\": 55.0}, {\"Id\":\"91a6471b-1426-43f6-ad65-6fc473e16f9f\", \"Name\":\"WaterSpell\", \"Damage\": 21.0}, {\"Id\":\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\", \"Name\":\"Ork\", \"Damage\": 55.0}, {\"Id\":\"f8043c23-1534-4487-b66b-238e0c3c39b5\", \"Name\":\"WaterSpell\",   \"Damage\": 23.0}]";
        String p2 = "[{\"Id\":\"b017ee50-1c14-44e2-bfd6-2c0c5653a37c\", \"Name\":\"WaterGoblin\", \"Damage\": 11.0}, {\"Id\":\"d04b736a-e874-4137-b191-638e0ff3b4e7\", \"Name\":\"Dragon\", \"Damage\": 70.0}, {\"Id\":\"88221cfe-1f84-41b9-8152-8e36c6a354de\", \"Name\":\"WaterSpell\", \"Damage\": 22.0}, {\"Id\":\"1d3f175b-c067-4359-989d-96562bfa382c\", \"Name\":\"Ork\", \"Damage\": 40.0}, {\"Id\":\"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\", \"Name\":\"RegularSpell\", \"Damage\": 28.0}]";
        String p3 = "[{\"Id\":\"ed1dc1bc-f0aa-4a0c-8d43-1402189b33c8\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"65ff5f23-1e70-4b79-b3bd-f6eb679dd3b5\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"55ef46c4-016c-4168-bc43-6b9b1e86414f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"f3fad0f2-a1af-45df-b80d-2e48825773d9\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"8c20639d-6400-4534-bd0f-ae563f11f57a\", \"Name\":\"WaterSpell\",   \"Damage\": 25.0}]";
        String p4 = "[{\"Id\":\"d7d0cb94-2cbf-4f97-8ccf-9933dc5354b8\", \"Name\":\"WaterGoblin\", \"Damage\":  9.0}, {\"Id\":\"44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e\", \"Name\":\"Dragon\", \"Damage\": 55.0}, {\"Id\":\"2c98cd06-518b-464c-b911-8d787216cddd\", \"Name\":\"WaterSpell\", \"Damage\": 21.0}, {\"Id\":\"951e886a-0fbf-425d-8df5-af2ee4830d85\", \"Name\":\"Ork\", \"Damage\": 55.0}, {\"Id\":\"dcd93250-25a7-4dca-85da-cad2789f7198\", \"Name\":\"FireSpell\",    \"Damage\": 23.0}]";
        String p5 = "[{\"Id\":\"b2237eca-0271-43bd-87f6-b22f70d42ca4\", \"Name\":\"WaterGoblin\", \"Damage\": 11.0}, {\"Id\":\"9e8238a4-8a7a-487f-9f7d-a8c97899eb48\", \"Name\":\"Dragon\", \"Damage\": 70.0}, {\"Id\":\"d60e23cf-2238-4d49-844f-c7589ee5342e\", \"Name\":\"WaterSpell\", \"Damage\": 22.0}, {\"Id\":\"fc305a7a-36f7-4d30-ad27-462ca0445649\", \"Name\":\"Ork\", \"Damage\": 40.0}, {\"Id\":\"84d276ee-21ec-4171-a509-c1b88162831c\", \"Name\":\"RegularSpell\", \"Damage\": 28.0}]";

        // Not admin
        assertEquals(403, packageController.createPackage(p0, authenticatedUser).getStatusCode());


        assertEquals(201, packageController.createPackage(p0, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p1, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p2, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p3, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p4, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p5, adminUser).getStatusCode());

        // Already created
        assertEquals(409, packageController.createPackage(p0, adminUser).getStatusCode());


        assertEquals(authenticatedUser.getCoins(), 20);
        assertEquals(200, packageController.buyPackage(authenticatedUser).getStatusCode());
        assertEquals(200, packageController.buyPackage(authenticatedUser).getStatusCode());
        assertEquals(200, packageController.buyPackage(authenticatedUser).getStatusCode());
        assertEquals(200, packageController.buyPackage(authenticatedUser).getStatusCode());
        assertEquals(authenticatedUser.getCoins(), 0);
        assertEquals(403, packageController.buyPackage(authenticatedUser).getStatusCode());

        Package aPackage = cardService.getPackages().get(0);
        assertEquals(authenticatedUser, aPackage.getUser());
        assertEquals(5, aPackage.getCards().size());
    }

    @Test
    @Order(3)
    void testCardController() throws JsonProcessingException {
        CardController cardController = new CardController(cardService, tradingService, cardRepositorySpy);
        assertEquals(200, cardController.getCards(authenticatedUser).getStatusCode());
        // Should own 20 cards
        assertEquals(20, cardService.getCardsFromUser(authenticatedUser).size());

        String d = "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]";
        String dWrongLength = "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]";
        String dWrongCard = "[\"aa9999a0-734c-49c6-8f4a-651864b14e62\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]";

        assertEquals(204, cardController.getCardsFromDeck(authenticatedUser, true).getStatusCode());

        assertEquals(400, cardController.putCardsIntoDeck(dWrongLength, authenticatedUser).getStatusCode());
        assertEquals(403, cardController.putCardsIntoDeck(dWrongCard, authenticatedUser).getStatusCode());


        assertEquals(200, cardController.putCardsIntoDeck(d, authenticatedUser).getStatusCode());
        assertEquals(200, cardController.getCardsFromDeck(authenticatedUser, true).getStatusCode());

        List<Card> cardsFromDeck = cardService.getCardsFromDeck(authenticatedUser);
        assertEquals("WaterGoblin", cardsFromDeck.get(0).getName());
        assertEquals("Dragon", cardsFromDeck.get(1).getName());
        assertEquals("WaterSpell", cardsFromDeck.get(2).getName());
        assertEquals("RegularSpell", cardsFromDeck.get(3).getName());

        String dDragon = "[\"4a2757d6-b1c3-47ac-b9a3-91deab093531\", \"d04b736a-e874-4137-b191-638e0ff3b4e7\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"ed1dc1bc-f0aa-4a0c-8d43-1402189b33c8\"]";
        assertEquals(200, cardController.putCardsIntoDeck(dDragon, authenticatedUser).getStatusCode());

        cardsFromDeck = cardService.getCardsFromDeck(authenticatedUser);
        Card card = cardsFromDeck.get(0);
        assertEquals("Dragon", card.getName());
        assertEquals(50.0, card.getDamage());
        assertEquals(CardType.MONSTER, card.getCardType());
        assertEquals(ElementType.NORMAL, card.getElementType());
        assertEquals(authenticatedUser, card.getOwner());
        assertTrue(card.isDragon());
        assertFalse(card.isGoblin());
    }


    @Test
    @Order(4)
    void testStatController() throws JsonProcessingException {
        StatController statController = new StatController(userService);

        assertEquals(200, statController.getStats(authenticatedUser).getStatusCode());
        assertEquals(200, statController.getScores().getStatusCode());

        List<Stat> scoreboard = userService.getScoreboard();
        assertEquals(2, scoreboard.size());
        Stat kienStat = scoreboard.get(0);

        assertEquals(100, kienStat.getElo());
        assertEquals(0, kienStat.getWins());
        assertEquals(0, kienStat.getDraws());
        assertEquals(0, kienStat.getDefeats());
    }
}
