package org.example.app.services;

import lombok.AccessLevel;
import lombok.Setter;
import org.example.app.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserService {
    @Setter(AccessLevel.PRIVATE)
    private List<User> users;

    public UserService() {
        setUsers(new ArrayList<>());
    }

    public User getUserById(String id) {
        User foundUser = users.stream()
                .filter(User -> Objects.equals(id, User.getId()))
                .findAny()
                .orElse(null);

        return foundUser;
    }

    public List<User> getUsers() {
        return users;
    }

    public boolean addUser(User user) {
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            return false;
        }

        return users.add(user);
    }

    public void removeUser(String id) {
        users.removeIf(User -> Objects.equals(id, User.getId()));
    }

    public boolean login(User proposedUser) {
        User foundUser = users.stream().filter(user -> proposedUser.getUsername().equals(user.getUsername())).findFirst().orElse(null);

        if (foundUser == null){
            return false;
        }

        return proposedUser.getPassword().equals(foundUser.getPassword());
    }

    public User getAuthenticatedUser(String token) {
        return users.stream().filter(user -> Objects.equals(user.getToken(), token)).findFirst().orElse(null);
    }
}
