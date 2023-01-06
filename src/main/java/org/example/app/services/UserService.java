package org.example.app.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.controllers.Controller;
import org.example.app.models.Stat;
import org.example.app.models.User;

import java.util.*;

public class UserService {
    @Getter(AccessLevel.PUBLIC)
    private final List<User> users = Collections.synchronizedList(new ArrayList<>());

    public UserService() {
    }

    public User getUser(String username) {
        return users.stream()
                .filter(user -> Objects.equals(user.getUsername(), username))
                .findFirst().orElse(null);
    }

    public User putUser(String username, JsonNode rootNode) {
        User user;
        synchronized (users) {
            user = users.stream()
                    .filter(u -> Objects.equals(u.getUsername(), username))
                    .findFirst().orElse(null);

            if (user == null) return null;
        }

        synchronized (user) {
            user.setName(Controller.getFieldValueCaseInsensitive(rootNode, "name"));
            user.setBio(Controller.getFieldValueCaseInsensitive(rootNode, "bio"));
            user.setImage(Controller.getFieldValueCaseInsensitive(rootNode, "image"));
        }

        return user;
    }


    public boolean addUser(User user) {
        synchronized (users) {
            if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
                return false;
            }

            return users.add(user);
        }
    }

    public boolean login(User proposedUser) {
        User foundUser = users.stream().filter(user -> proposedUser.getUsername().equals(user.getUsername())).findFirst().orElse(null);

        if (foundUser == null) {
            return false;
        }

        // Both should already be hashed at this point
        boolean loginSuccessful = proposedUser.getPasswordHash().equals(foundUser.getPasswordHash());
        if (loginSuccessful) {
            synchronized (foundUser) {
                foundUser.generateToken();
            }
        }

        return loginSuccessful;
    }

    public User getAuthenticatedUser(String token) {
        return users.stream().filter(user -> Objects.equals(user.getToken(), token)).findFirst().orElse(null);
    }


    public Stat getStats(User authenticatedUser) {
        return authenticatedUser.getStat();
    }

    public List<Stat> getScoreboard() {
        return users.stream().map(User::getStat).sorted(Comparator.comparingInt(value -> -value.getElo())).toList();
    }
}
