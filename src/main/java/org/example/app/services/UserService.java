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
}
