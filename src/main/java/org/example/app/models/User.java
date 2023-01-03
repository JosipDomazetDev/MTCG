package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.app.services.exceptions.NoMoneyException;

import java.beans.ConstructorProperties;
import java.util.UUID;

@Getter
@Setter
public class User {
    private String id;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String username;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonIgnore
    private String password;

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
    private Stack stack = new Stack(this);


    @ConstructorProperties({"username", "password"})
    public User(String username, String password) {
        this.id = UUID.randomUUID().toString();
        this.username = username.toLowerCase();
        this.password = password;
        this.token = this.username + "-mtcgToken";
        this.coins = 20;
        this.name = null;
        this.bio = null;
        this.image = null;

        if (username.equals("admin")) {
            isAdmin = true;
        }
    }

    public User(String id, String username, String token, int coins, String name, String bio, String image) {
        this.id = id;
        this.username = username;
        this.password = null;
        this.token = token;
        this.coins = coins;
        this.name = name;
        this.bio = bio;
        this.image = image;
    }


    public void buyPackage(Package packageToBeBought) throws NoMoneyException {
        if(packageToBeBought.getPrice() > coins){
            throw new NoMoneyException();
        }

        packageToBeBought.setUser(this);
        setCoins(getCoins() - packageToBeBought.getPrice());
    }
}
