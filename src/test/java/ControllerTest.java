import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.app.controllers.*;
import org.example.app.models.Package;
import org.example.app.models.*;
import org.example.app.models.enums.CardType;
import org.example.app.models.enums.ElementType;
import org.example.app.repositories.*;
import org.example.app.services.BattleService;
import org.example.app.services.CardService;
import org.example.app.services.TradingService;
import org.example.app.services.UserService;
import org.example.app.utils.PasswordUtils;
import org.example.server.Response;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerTest {
    User kienboecUser;
    User adminUser;

    ConnectionPool mockConnectionPool = mock(ConnectionPool.class);
    Connection mockConnection = mock(Connection.class);

    UserRepository userRepositorySpy;
    CardRepository cardRepositorySpy;
    BattleRepository battleRepositorySpy;
    TradeRepository tradeRepositorySpy;

    CardService cardService = new CardService();
    TradingService tradingService = new TradingService();
    UserService userService = new UserService();
    BattleService battleService = new BattleService();

    UserController userController;
    PackageController packageController;
    CardController cardController;


    public ControllerTest() {
    }

    @BeforeAll
    void setupController() {
        userRepositorySpy = Mockito.spy(mock(UserRepository.class));
        Mockito.doNothing().when(userRepositorySpy).insert(Mockito.any());
        Mockito.doNothing().when(userRepositorySpy).update(Mockito.any());


        cardRepositorySpy = Mockito.spy(mock(CardRepository.class));
        Mockito.doNothing().when(cardRepositorySpy).insert(Mockito.any());
        Mockito.doNothing().when(cardRepositorySpy).update(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(cardRepositorySpy).updateDeck(Mockito.any());


        battleRepositorySpy = Mockito.spy(mock(BattleRepository.class));
        Mockito.doNothing().when(battleRepositorySpy).insert(Mockito.any());

        tradeRepositorySpy = Mockito.spy(mock(TradeRepository.class));
        Mockito.doNothing().when(tradeRepositorySpy).delete(Mockito.any());
        Mockito.doNothing().when(tradeRepositorySpy).performTrade(Mockito.any());
        Mockito.doNothing().when(tradeRepositorySpy).insert(Mockito.any());

        // Mock executeQuery to pass mock connection
        doAnswer(invocation -> {
            RunnableConnection runnable = invocation.getArgument(0);
            runnable.run(mockConnection);
            return null;
        }).when(mockConnectionPool).executeQuery(any(RunnableConnection.class));


        userController = new UserController(userService, userRepositorySpy);
        packageController = new PackageController(cardService, cardRepositorySpy);
        cardController = new CardController(cardService, tradingService, cardRepositorySpy);
    }


    @Test
    @Order(1)
    void testUserAndSessionController() throws JsonProcessingException {
        SessionController sessionController = new SessionController(userService);

        Response create = userController.createUser("{\"Username\":\"kienboec\", \"Password\":\"daniel\"}");
        assertEquals(201, create.getStatusCode());

        Response login = sessionController.login("{\"Username\":\"kienboec\", \"Password\":\"WRONG\"}");
        assertEquals(401, login.getStatusCode());

        login = sessionController.login("{\"Username\":\"kienboec\", \"Password\":\"daniel\"}");
        assertEquals(200, login.getStatusCode());

        User authenticatedUser1 = sessionController.getAuthenticatedUser("kienboec-mtcgToken");
        assertEquals(authenticatedUser1.getUsername(), "kienboec");
        kienboecUser = authenticatedUser1;

        Response put = userController.putUser("{\"Name\": \"Kienboeck\",  \"Bio\": \"me playin...\", \"Image\": \":-)\"}", "kienboec", kienboecUser);
        assertEquals(200, put.getStatusCode());

        Response get = userController.getUser("kienboec", kienboecUser);
        assertEquals(200, get.getStatusCode());
        User kienboecUser1 = userService.getUser("kienboec");

        assertEquals("kienboec", kienboecUser1.getUsername());
        assertEquals("Kienboeck", kienboecUser1.getName());
        assertEquals("me playin...", kienboecUser1.getBio());
        assertEquals(":-)", kienboecUser1.getImage());
        assertEquals(20, kienboecUser1.getCoins());
    }

    @Test
    @Order(2)
    public void testPasswordHashing() throws NoSuchAlgorithmException {
        User user = new User("test", "passwort");

        assertEquals(PasswordUtils.hashPassword("passwort".toCharArray()), user.getPasswordHash());

        assertNotEquals(PasswordUtils.hashPassword("Passwort".toCharArray()), user.getPasswordHash());
        assertNotEquals(PasswordUtils.hashPassword("other".toCharArray()), user.getPasswordHash());
    }

    @Test
    @Order(3)
    public void testUserStatConstructor() throws NoSuchAlgorithmException {
        User user = new User("test", "passwort");

        Stat stat = new Stat(user);
        assertEquals(100, stat.getElo());
        assertEquals(user, stat.getUser());

        stat = new Stat(user.getId(), 100, 0, 1, 1);
        assertEquals(1, stat.getTotal());
        assertNull(stat.getUser());

        stat.assignUser(user);
        assertEquals(user, stat.getUser());
        assertNull(stat.getFkUserId());
    }

    @Test
    @Order(4)
    public void testAdminUser() throws JsonProcessingException {
        userController.createUser("{\"Username\":\"admin\",    \"Password\":\"istrator\"}");
        adminUser = userService.getUser("admin");
        assertFalse(kienboecUser.isAdmin());
        assertTrue(adminUser.isAdmin());
    }


    @Test
    @Order(5)
    void testPackageController() throws JsonProcessingException {

        String p0 = "[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]";
        String p1 = "[{\"Id\":\"644808c2-f87a-4600-b313-122b02322fd5\", \"Name\":\"WaterGoblin\", \"Damage\":  9.0}, {\"Id\":\"4a2757d6-b1c3-47ac-b9a3-91deab093531\", \"Name\":\"Dragon\", \"Damage\": 55.0}, {\"Id\":\"91a6471b-1426-43f6-ad65-6fc473e16f9f\", \"Name\":\"WaterSpell\", \"Damage\": 21.0}, {\"Id\":\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\", \"Name\":\"Ork\", \"Damage\": 55.0}, {\"Id\":\"f8043c23-1534-4487-b66b-238e0c3c39b5\", \"Name\":\"WaterSpell\",   \"Damage\": 23.0}]";
        String p2 = "[{\"Id\":\"b017ee50-1c14-44e2-bfd6-2c0c5653a37c\", \"Name\":\"WaterGoblin\", \"Damage\": 11.0}, {\"Id\":\"d04b736a-e874-4137-b191-638e0ff3b4e7\", \"Name\":\"Dragon\", \"Damage\": 70.0}, {\"Id\":\"88221cfe-1f84-41b9-8152-8e36c6a354de\", \"Name\":\"WaterSpell\", \"Damage\": 22.0}, {\"Id\":\"1d3f175b-c067-4359-989d-96562bfa382c\", \"Name\":\"Ork\", \"Damage\": 40.0}, {\"Id\":\"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\", \"Name\":\"RegularSpell\", \"Damage\": 28.0}]";
        String p3 = "[{\"Id\":\"ed1dc1bc-f0aa-4a0c-8d43-1402189b33c8\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"65ff5f23-1e70-4b79-b3bd-f6eb679dd3b5\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"55ef46c4-016c-4168-bc43-6b9b1e86414f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"f3fad0f2-a1af-45df-b80d-2e48825773d9\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"8c20639d-6400-4534-bd0f-ae563f11f57a\", \"Name\":\"WaterSpell\",   \"Damage\": 25.0}]";
        String p4 = "[{\"Id\":\"d7d0cb94-2cbf-4f97-8ccf-9933dc5354b8\", \"Name\":\"WaterGoblin\", \"Damage\":  9.0}, {\"Id\":\"44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e\", \"Name\":\"Dragon\", \"Damage\": 55.0}, {\"Id\":\"2c98cd06-518b-464c-b911-8d787216cddd\", \"Name\":\"WaterSpell\", \"Damage\": 21.0}, {\"Id\":\"951e886a-0fbf-425d-8df5-af2ee4830d85\", \"Name\":\"Ork\", \"Damage\": 55.0}, {\"Id\":\"dcd93250-25a7-4dca-85da-cad2789f7198\", \"Name\":\"FireSpell\",    \"Damage\": 23.0}]";
        String p5 = "[{\"Id\":\"b2237eca-0271-43bd-87f6-b22f70d42ca4\", \"Name\":\"WaterGoblin\", \"Damage\": 11.0}, {\"Id\":\"9e8238a4-8a7a-487f-9f7d-a8c97899eb48\", \"Name\":\"Dragon\", \"Damage\": 70.0}, {\"Id\":\"d60e23cf-2238-4d49-844f-c7589ee5342e\", \"Name\":\"WaterSpell\", \"Damage\": 22.0}, {\"Id\":\"fc305a7a-36f7-4d30-ad27-462ca0445649\", \"Name\":\"Ork\", \"Damage\": 40.0}, {\"Id\":\"84d276ee-21ec-4171-a509-c1b88162831c\", \"Name\":\"RegularSpell\", \"Damage\": 28.0}]";

        // Not admin
        assertEquals(403, packageController.createPackage(p0, kienboecUser).getStatusCode());


        assertEquals(201, packageController.createPackage(p0, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p1, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p2, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p3, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p4, adminUser).getStatusCode());
        assertEquals(201, packageController.createPackage(p5, adminUser).getStatusCode());

        // Already created
        assertEquals(409, packageController.createPackage(p0, adminUser).getStatusCode());


        assertEquals(kienboecUser.getCoins(), 20);
        assertEquals(200, packageController.buyPackage(kienboecUser).getStatusCode());
        assertEquals(200, packageController.buyPackage(kienboecUser).getStatusCode());
        assertEquals(200, packageController.buyPackage(kienboecUser).getStatusCode());
        assertEquals(200, packageController.buyPackage(kienboecUser).getStatusCode());
        assertEquals(kienboecUser.getCoins(), 0);
        assertEquals(403, packageController.buyPackage(kienboecUser).getStatusCode());

    }

    @Test
    @Order(6)
    public void testBuyPackagesForAdmin() throws JsonProcessingException {
        Package aPackage = cardService.getPackages().get(0);
        assertEquals(kienboecUser, aPackage.getUser());
        assertEquals(5, aPackage.getCards().size());

        // Buy for Admin
        assertEquals(200, packageController.buyPackage(adminUser).getStatusCode());
        assertEquals(200, packageController.buyPackage(adminUser).getStatusCode());
        // No cards
        assertEquals(404, packageController.buyPackage(adminUser).getStatusCode());
    }

    @Test
    @Order(7)
    void testCardController() throws JsonProcessingException {
        assertEquals(200, cardController.getCards(kienboecUser).getStatusCode());
        // Should own 20 cards
        assertEquals(20, cardService.getCardsFromUser(kienboecUser).size());

        String d = "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]";
        String dWrongLength = "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]";
        String dWrongCard = "[\"aa9999a0-734c-49c6-8f4a-651864b14e62\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]";

        assertEquals(204, cardController.getCardsFromDeck(kienboecUser, true).getStatusCode());

        assertEquals(400, cardController.putCardsIntoDeck(dWrongLength, kienboecUser).getStatusCode());
        assertEquals(403, cardController.putCardsIntoDeck(dWrongCard, kienboecUser).getStatusCode());


        assertEquals(200, cardController.putCardsIntoDeck(d, kienboecUser).getStatusCode());
        assertEquals(200, cardController.getCardsFromDeck(kienboecUser, true).getStatusCode());

        List<Card> cardsFromDeck = cardService.getCardsFromDeck(kienboecUser);
        assertEquals("WaterGoblin", cardsFromDeck.get(0).getName());
        assertEquals("Dragon", cardsFromDeck.get(1).getName());
        assertEquals("WaterSpell", cardsFromDeck.get(2).getName());
        assertEquals("RegularSpell", cardsFromDeck.get(3).getName());

        String dDragon = "[\"4a2757d6-b1c3-47ac-b9a3-91deab093531\", \"d04b736a-e874-4137-b191-638e0ff3b4e7\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"ed1dc1bc-f0aa-4a0c-8d43-1402189b33c8\"]";
        assertEquals(200, cardController.putCardsIntoDeck(dDragon, kienboecUser).getStatusCode());

        cardsFromDeck = cardService.getCardsFromDeck(kienboecUser);
        Card card = cardsFromDeck.get(0);
        assertEquals("Dragon", card.getName());
        assertEquals(50.0, card.getDamage());
        assertEquals(CardType.MONSTER, card.getCardType());
        assertEquals(ElementType.NORMAL, card.getElementType());
        assertEquals(kienboecUser, card.getOwner());
        assertTrue(card.isDragon());
        assertFalse(card.isGoblin());
    }

    @Test
    @Order(8)
    void testDeckAdminUser() throws JsonProcessingException {
        String dAdmin = "[\"d7d0cb94-2cbf-4f97-8ccf-9933dc5354b8\", \"44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e\", \"2c98cd06-518b-464c-b911-8d787216cddd\", \"951e886a-0fbf-425d-8df5-af2ee4830d85\"]";
        assertEquals(200, cardController.putCardsIntoDeck(dAdmin, adminUser).getStatusCode());
        assertEquals(4, adminUser.getDeck().getCards().size());
    }

    @Test
    @Order(9)
    void testStatController() throws JsonProcessingException {
        StatController statController = new StatController(userService);

        assertEquals(200, statController.getStats(kienboecUser).getStatusCode());
        assertEquals(200, statController.getScores().getStatusCode());

        List<Stat> scoreboard = userService.getScoreboard();
        assertEquals(2, scoreboard.size());
        Stat kienStat = scoreboard.get(0);

        assertEquals(100, kienStat.getElo());
        assertEquals(0, kienStat.getWins());
        assertEquals(0, kienStat.getDraws());
        assertEquals(0, kienStat.getDefeats());
    }

    void executeWithFixedSeed(RunnableWithException action) throws NoSuchAlgorithmException, JsonProcessingException, InterruptedException {
        Random random = new Random();
        random.setSeed(1);
        try (MockedStatic<Battle> mocked = mockStatic(Battle.class)) {
            mocked.when(Battle::getRand).thenReturn(random);
            action.run();
        }
    }

    @Test
    @Order(10)
    void testBattleLogicMonster() throws JsonProcessingException, InterruptedException, NoSuchAlgorithmException {
        executeWithFixedSeed(() -> {
            Battle battle = new Battle(kienboecUser);
            battle.setPlayer2(adminUser);

            Card card1 = new Card("bla", "WaterGoblin", 10, 0, 0);
            Card card2 = new Card("bla", "FireTroll", 15, 0, 0);
            assertEquals(card2, battle.battle(card1, card2));

            card1 = new Card("bla", "FireTroll", 15, 0, 0);
            card2 = new Card("bla", "WaterGoblin", 10, 0, 0);
            assertEquals(card1, battle.battle(card1, card2));

            card1 = new Card("bla", "FireTroll", 15, 0, 0);
            card2 = new Card("bla", "WaterGoblin", 10, 0, 0);
            assertEquals(card1, battle.battle(card1, card2));
        });
    }

    @Test
    @Order(11)
    void testBattleLogicSpell() throws JsonProcessingException, InterruptedException, NoSuchAlgorithmException {
        executeWithFixedSeed(() -> {
            Battle battle = new Battle(kienboecUser);
            battle.setPlayer2(adminUser);

            Card card1 = new Card("bla", "FireSpell", 10);
            Card card2 = new Card("bla", "WaterSpell", 20);
            assertEquals(card2, battle.battle(card1, card2));

            card1 = new Card("bla", "FireSpell", 20);
            card2 = new Card("bla", "WaterSpell", 5);
            assertNull(battle.battle(card1, card2));

            card1 = new Card("bla", "FireSpell", 90);
            card2 = new Card("bla", "WaterSpell", 5);
            assertEquals(card1, battle.battle(card1, card2));
        });
    }

    @Test
    @Order(12)
    void testBattleLogicMixed() throws JsonProcessingException, InterruptedException, NoSuchAlgorithmException {
        executeWithFixedSeed(() -> {
            Battle battle = new Battle(kienboecUser);
            battle.setPlayer2(adminUser);

            Card card1 = new Card("bla", "FireSpell", 10);
            Card card2 = new Card("bla", "WaterGoblin", 10);
            assertEquals(card2, battle.battle(card1, card2));

            card1 = new Card("bla", "WaterSpell", 10);
            card2 = new Card("bla", "WaterGoblin", 10);
            assertNull(battle.battle(card1, card2));

            card1 = new Card("bla", "RegularSpell", 10);
            card2 = new Card("bla", "WaterGoblin", 10);
            assertEquals(card1, battle.battle(card1, card2));

            card1 = new Card("bla", "RegularSpell", 10);
            card2 = new Card("bla", "Knight", 15);
            assertEquals(card2, battle.battle(card1, card2));
        });
    }

    @Test
    @Order(13)
    void testBattleLogicCrit() throws JsonProcessingException, InterruptedException, NoSuchAlgorithmException {
        executeWithFixedSeed(() -> {
            Battle battle = new Battle(kienboecUser);
            battle.setPlayer2(adminUser);

            Card card1 = new Card("bla", "Dragon", 10, 1, 0);
            Card card2 = new Card("bla", "Knight", 15, 0, 0);

            // Draw due to crit
            assertNull(battle.battle(card1, card2));

            card1 = new Card("bla", "Dragon", 15, 1, 0);
            card2 = new Card("bla", "Knight", 15, 0, 0);

            assertEquals(card1, battle.battle(card1, card2));
        });
    }


    @Test
    @Order(14)
    void testBattleLogicDodge() throws JsonProcessingException, InterruptedException, NoSuchAlgorithmException {
        executeWithFixedSeed(() -> {
            Battle battle = new Battle(kienboecUser);
            battle.setPlayer2(adminUser);

            Card card1 = new Card("bla", "Dragon", 1, 0, 1);
            Card card2 = new Card("bla", "Knight", 99, 0, 0);

            // Draw due to dodge
            assertNull(battle.battle(card1, card2));

            card1 = new Card("bla", "Dragon", 1, 0, 1);
            card2 = new Card("bla", "Knight", 99, 0, 1);

            assertNull(battle.battle(card1, card2));
        });
    }


    @Test
    @Order(15)
    void testBattleLogicSpecial() throws JsonProcessingException, InterruptedException, NoSuchAlgorithmException {
        executeWithFixedSeed(() -> {
            Battle battle = new Battle(kienboecUser);
            battle.setPlayer2(adminUser);

            Card card1 = new Card("bla", "Dragon", 1);
            Card card2 = new Card("bla", "WaterGoblin", 100);
            assertEquals(card1, battle.battle(card1, card2));

            card1 = new Card("bla", "Wizzard", 1);
            card2 = new Card("bla", "Ork", 100);
            assertEquals(card1, battle.battle(card1, card2));

            card1 = new Card("bla", "WaterSpell", 1);
            card2 = new Card("bla", "Knight", 100);
            assertEquals(card1, battle.battle(card1, card2));

            card1 = new Card("bla", "RegularSpell", 100);
            card2 = new Card("bla", "Kraken", 1);
            assertEquals(card2, battle.battle(card1, card2));

            card1 = new Card("bla", "FireElf", 1);
            card2 = new Card("bla", "Dragon", 100);
            assertEquals(card1, battle.battle(card1, card2));
        });
    }

    private void performBattle(BattleController battleController) throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                battleController.createOrStartBattle(kienboecUser);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        Thread.sleep(100);

        assertEquals(200, battleController.createOrStartBattle(adminUser).getStatusCode());
    }

    @Test
    @Order(16)
    void testBattleController() throws JsonProcessingException, InterruptedException, NoSuchAlgorithmException {
        BattleController battleController = new BattleController(battleService, battleRepositorySpy);
        // Empty deck
        assertEquals(404, battleController.createOrStartBattle(new User("test", "test")).getStatusCode());

        executeWithFixedSeed(() -> {
            performBattle(battleController);
            assertEquals(95, adminUser.getStat().getElo());
            assertEquals(0, adminUser.getStat().getWins());
            assertEquals(0, adminUser.getStat().getDraws());
            assertEquals(1, adminUser.getStat().getDefeats());

            performBattle(battleController);
            performBattle(battleController);

            assertEquals(85, adminUser.getStat().getElo());
            assertEquals(0, adminUser.getStat().getWins());
            assertEquals(0, adminUser.getStat().getDraws());
            assertEquals(3, adminUser.getStat().getDefeats());

            assertEquals(109, kienboecUser.getStat().getElo());
            assertEquals(3, kienboecUser.getStat().getWins());
            assertEquals(0, kienboecUser.getStat().getDraws());
            assertEquals(0, kienboecUser.getStat().getDefeats());
        });

    }

    @Test
    @Order(17)
    void testJSONFormats() throws JsonProcessingException, NoSuchAlgorithmException {
        BattleController battleController = new BattleController(battleService, battleRepositorySpy);
        ObjectMapper objectMapper = battleController.getObjectMapper();

        String cardJson = "{\"id\":\"bla\",\"name\":\"Dragon\",\"damage\":1.0}";
        assertEquals(cardJson, objectMapper.writeValueAsString(new Card("bla", "Dragon", 1)));

        String userJson = "{\"username\":\"kienboec\",\"coins\":0,\"name\":\"Kienboeck\",\"bio\":\"me playin...\",\"image\":\":-)\"}";
        assertEquals(userJson, objectMapper.writeValueAsString(kienboecUser));

        String tradeJson = "{\"id\":\"bla\",\"cardToTrade\":\"cardid\",\"type\":\"Monster\",\"minimumDamage\":70.0}";
        assertEquals(tradeJson, objectMapper.writeValueAsString(new Trade("bla", "cardid", "monster", 70)));

        String statJson = "{\"elo\":100,\"wins\":0,\"draws\":0,\"name\":null,\"losses\":0,\"winRate\":\"0%\"}";
        assertEquals(statJson, objectMapper.writeValueAsString(new Stat(new User("bla", "bla"))));
    }


    @Test
    @Order(18)
    void testErrorController() {
        assertEquals(401, ErrorController.sendUnauthorized().getStatusCode());
    }

    @Test
    @Order(19)
    void testTradeController() throws JsonProcessingException {
        TradingController tradingController = new TradingController(tradingService, cardService, tradeRepositorySpy);

        String t = "{\"Id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\", \"CardToTrade\": \"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Type\": \"monster\", \"MinimumDamage\": 15}";


        assertEquals(201, tradingController.postTrades(t, kienboecUser).getStatusCode());

        Trade trade = tradingService.getAllTrades().get(0);
        assertEquals("6cd85277-4590-49d4-b0cf-ba0a921faad0", trade.getId());
        assertEquals("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", trade.getCard().getId());
        assertEquals(15, trade.getMinimumDamage());
        assertEquals(CardType.MONSTER, trade.getCardType());
        assertNull(trade.getUser2());
        assertFalse(trade.isCompleted());


        //The provided deal ID was not found.
        assertEquals(404, tradingController.deleteTrade(kienboecUser, "wrong").getStatusCode());
        //The deal contains a card that is not owned by the user.
        assertEquals(403, tradingController.deleteTrade(adminUser, "6cd85277-4590-49d4-b0cf-ba0a921faad0").getStatusCode());
        assertEquals(200, tradingController.deleteTrade(kienboecUser, "6cd85277-4590-49d4-b0cf-ba0a921faad0").getStatusCode());
        assertEquals(0, tradingService.getAllTrades().size());


        assertEquals(201, tradingController.postTrades(t, kienboecUser).getStatusCode());

        // trade id not found
        assertEquals(404, tradingController.performTrade("\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\"", kienboecUser, "wrong").getStatusCode());
        // try to trade with yourself (should fail)
        assertEquals(403, tradingController.performTrade("\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\"", kienboecUser, "6cd85277-4590-49d4-b0cf-ba0a921faad0").getStatusCode());
        // card not owned by user
        assertEquals(403, tradingController.performTrade("\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\"", adminUser, "6cd85277-4590-49d4-b0cf-ba0a921faad0").getStatusCode());
        // card does not fulfill requirements
        assertEquals(403, tradingController.performTrade("\"84d276ee-21ec-4171-a509-c1b88162831c\"", adminUser, "6cd85277-4590-49d4-b0cf-ba0a921faad0").getStatusCode());
        // Should work
        String dragonIdOwnedByAdminOriginally = "9e8238a4-8a7a-487f-9f7d-a8c97899eb48";
        assertEquals(200, tradingController.performTrade("\"" + dragonIdOwnedByAdminOriginally + "\"", adminUser, "6cd85277-4590-49d4-b0cf-ba0a921faad0").getStatusCode());


        trade = tradingService.getAllTrades().get(0);
        assertEquals("6cd85277-4590-49d4-b0cf-ba0a921faad0", trade.getId());
        assertEquals("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", trade.getCard().getId());
        assertEquals(15, trade.getMinimumDamage());
        assertEquals(CardType.MONSTER, trade.getCardType());
        assertEquals(adminUser, trade.getUser2());
        assertTrue(trade.isCompleted());

        Card newCardKien = cardService.getCardsFromUser(kienboecUser).stream().filter(card -> Objects.equals(card.getId(), dragonIdOwnedByAdminOriginally)).findFirst().orElse(null);
        assertEquals("Dragon", newCardKien.getName());

        Card oldCardAdmin = cardService.getCardsFromUser(adminUser).stream().filter(card -> Objects.equals(card.getId(), dragonIdOwnedByAdminOriginally)).findFirst().orElse(null);
        assertNull(oldCardAdmin);
    }


    @Test
    @Order(20)
    public void testLoadAllBattles() throws SQLException {
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(Mockito.any())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);


        User player1 = userService.getUsers().get(0);
        User player2 = userService.getUsers().get(1);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString(1)).thenReturn("52c3d3b1-b3d9-4d36-a305-91a1f9242971");
        when(mockResultSet.getString(2)).thenReturn(player1.getId());
        when(mockResultSet.getString(3)).thenReturn(player2.getId());
        when(mockResultSet.getString(4)).thenReturn("bla");
        when(mockResultSet.getString(5)).thenReturn("DRAW");

        BattleRepository battleRepository = new BattleRepository(mockConnectionPool);
        List<Battle> battles = battleRepository.loadAll(userService.getUsers());

        assertEquals(1, battles.size());
        Battle battle = battles.get(0);
        assertEquals(player1, battle.getPlayer1());
        assertEquals(player2, battle.getPlayer2());
        assertEquals(battle.getBattleOutcome(), battle.getBattleOutcome());
    }

    @Test
    @Order(21)
    public void testLoadAllCard() throws SQLException {
        CardRepository cardRepository = new CardRepository(mockConnectionPool);

        PreparedStatement mockPreparedStatementCard = mock(PreparedStatement.class);
        ResultSet mockResultSetCard = mock(ResultSet.class);

        PreparedStatement mockPreparedStatementPack = mock(PreparedStatement.class);
        ResultSet mockResultSetPack = mock(ResultSet.class);

        PreparedStatement mockPreparedStatementDeck = mock(PreparedStatement.class);
        ResultSet mockResultSetDeck = mock(ResultSet.class);

        when(cardRepository.createSelectCardStatement(mockConnection)).thenReturn(mockPreparedStatementCard);
        when(mockPreparedStatementCard.executeQuery()).thenReturn(mockResultSetCard);

        when(cardRepository.createSelectPackStatement(mockConnection)).thenReturn(mockPreparedStatementPack);
        when(mockPreparedStatementPack.executeQuery()).thenReturn(mockResultSetPack);

        when(cardRepository.createSelectDeckStatement(mockConnection)).thenReturn(mockPreparedStatementDeck);
        when(mockPreparedStatementDeck.executeQuery()).thenReturn(mockResultSetDeck);


        Package aPackage = cardService.getPackages().get(0);

        when(mockResultSetPack.next()).thenReturn(true, false);
        when(mockResultSetPack.getString(1)).thenReturn(aPackage.getId());
        when(mockResultSetPack.getInt(2)).thenReturn(aPackage.getPrice());
        when(mockResultSetPack.getString(3)).thenReturn(aPackage.getUser().getId());


        Card packageCard = aPackage.getCards().get(0);

        when(mockResultSetCard.next()).thenReturn(true, false);
        when(mockResultSetCard.getString(1)).thenReturn(packageCard.getId());
        when(mockResultSetCard.getString(2)).thenReturn(packageCard.getName());
        when(mockResultSetCard.getDouble(3)).thenReturn(packageCard.getDamage());
        when(mockResultSetCard.getString(4)).thenReturn("WATER");
        when(mockResultSetCard.getString(5)).thenReturn("MONSTER");
        when(mockResultSetCard.getString(6)).thenReturn(packageCard.getOwner().getId());
        when(mockResultSetCard.getString(7)).thenReturn(packageCard.getPack().getId());


        Card deckCard = kienboecUser.getDeck().getCards().get(0);

        when(mockResultSetCard.next()).thenReturn(true, false);
        when(mockResultSetDeck.getString(1)).thenReturn(kienboecUser.getId());
        when(mockResultSetDeck.getString(2)).thenReturn(deckCard.getId());


        Package packageFromDb = cardRepository.loadAll(userService.getUsers()).get(0);
        // Check correct package owner
        assertEquals("kienboec", packageFromDb.getUser().getUsername());
        // Check correct card owner
        assertEquals("kienboec", packageFromDb.getCards().get(0).getOwner().getUsername());
        // Check correct card damage
        assertEquals(packageCard.getDamage(), packageFromDb.getCards().get(0).getDamage());
        // Check correct card is in package
        assertTrue(packageFromDb.getCards().stream().anyMatch(card -> Objects.equals(card.getId(), packageCard.getId())));
        // Check correct card is in deck
        assertTrue(packageFromDb.getUser().getDeck().getCards().stream().anyMatch(card -> Objects.equals(card.getId(), deckCard.getId())));
    }
}


interface RunnableWithException {
    void run() throws InputMismatchException, JsonProcessingException, NoSuchAlgorithmException, InterruptedException;
}
