package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.app.services.exceptions.NoMoneyException;

import java.beans.ConstructorProperties;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Getter
@Setter
public class User {
    private String id;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String username;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonIgnore
    private String passwordHash;

    private String token;

    private int coins;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String name;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String bio;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String image;

    private boolean isAdmin = false;

    @JsonIgnore
    private Deck deck = new Deck(this);

    @JsonIgnore
    private Stat stat = new Stat(this);


    @ConstructorProperties({"username", "password"})
    public User(String username, String password) throws NoSuchAlgorithmException {
        this.id = UUID.randomUUID().toString();
        this.username = username.toLowerCase();
        this.passwordHash = PasswordUtils.hashPassword(password.toCharArray());
        this.coins = 20;
        this.name = null;
        this.bio = null;
        this.image = null;

        finishConstructing(username);
    }


    public User(String id, String passwordHash, int coins, String username, String name, String bio, String image, Stat stat) {
        this.id = id;
        this.passwordHash = passwordHash;
        this.coins = coins;
        this.username = username;
        this.name = name;
        this.bio = bio;
        this.image = image;

        this.stat = stat == null ? new Stat(this) : stat.assignUser(this);

        finishConstructing(username);
    }

    public void generateToken() {
        // In Reality use some lib here
        this.token = this.username + "-mtcgToken";
    }

    private void finishConstructing(String username) {
        if (username.equals("admin")) {
            isAdmin = true;
        }

        generateToken();
    }


    public void buyPackage(Package packageToBeBought) throws NoMoneyException {
        if (packageToBeBought.getPrice() > coins) {
            throw new NoMoneyException();
        }

        packageToBeBought.setUser(this);
        setCoins(getCoins() - packageToBeBought.getPrice());

        for (Card card : packageToBeBought.getCards()) {
            // Set the new owner
            card.setOwner(this);
        }
    }

}
