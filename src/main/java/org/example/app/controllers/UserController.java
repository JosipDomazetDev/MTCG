package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.User;
import org.example.app.repositories.UserRepository;
import org.example.app.services.UserService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;

import java.util.List;
import java.util.Objects;

public class UserController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private UserService userService;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        setUserService(userService);
        setUserRepository(userRepository);
    }

    public Response putUser(String requestBody, String username, User authenticatedUser) throws JsonProcessingException {
        if (checkGivenUsernameIsntAuthenticatedUsername(username, authenticatedUser)) {
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"error\": \"User not found!\"}"
            );
        }

        JsonNode rootNode = getObjectMapper().readTree(requestBody);
        User user = getUserService().putUser(username, rootNode);

        if (user == null) {
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"error\": \"User not found!\"}"
            );
        }

        userRepository.update(user);
        String userJson = getObjectMapper().writeValueAsString(user);

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                userJson
        );
    }

    public Response getUser(String username, User authenticatedUser) throws JsonProcessingException {
        if (checkGivenUsernameIsntAuthenticatedUsername(username, authenticatedUser)) {
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"error\": \"User not found!\"}"
            );
        }

        User user = getUserService().getUser(username);

        if (user == null) {
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"error\": \"User not found!\"}"
            );
        }

        String userJson = getObjectMapper().writeValueAsString(user);

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                userJson
        );
    }

    private static boolean checkGivenUsernameIsntAuthenticatedUsername(String username, User authenticatedUser) {
        return !Objects.equals(authenticatedUser.getUsername(), username);
    }


    // POST /cities
    public Response createUser(String requestBody) throws JsonProcessingException {
        User user = getObjectMapper().readValue(requestBody, User.class);
        boolean isUserAdded = getUserService().addUser(user);

        if (!isUserAdded) {
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.JSON,
                    "{\"error\": \"User already created.\" }"
            );
        }

        userRepository.insert(user);

        return new Response(
                HttpStatus.CREATED,
                ContentType.JSON,
                getObjectMapper().writeValueAsString(user)
        );
    }

    public void loadAll() {
        List<User> userList = getUserRepository().loadAll();
        userService.getUsers().addAll(userList);
    }
}
