package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.User;
import org.example.app.services.UserService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;

import java.util.List;

public class UserController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private UserService userService;

    public UserController(UserService userService) {
        setUserService(userService);
    }

    public Response getUsers() {
        try {
            List<User> UserData = getUserService().getUsers();
            String UserDataJSON = getObjectMapper().writeValueAsString(UserData);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"data\": " + UserDataJSON + ", \"error\": null }"
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"error\": \"Illegal JSON-Format!\", \"data\": null }"
            );
        }
    }

    // GET /cities/:id
    public void getUserById(int id) {

    }

    // POST /cities
    public Response createUser(Request request) {
        try {
            User user = getObjectMapper().readValue(request.getBody(), User.class);
            boolean isUserAdded = getUserService().addUser(user);

            if (!isUserAdded) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"data\": null, \"error\": \"User already created.\" }"
                );
            }

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"data\": " + getObjectMapper().writeValueAsString(user) + ", \"error\": null }"
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"error\": \"Illegal JSON-Format!\", \"data\": null }"
            );
        }

    }

    // DELETE /cities/:id
    public void deleteUser(int id) {

    }
}
