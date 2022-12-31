package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.User;
import org.example.app.services.UserService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
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
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"Internal Server Error\", \"data\": null }"
            );
        }
    }

    // GET /cities/:id
    public void getUserById(int id) {

    }

    // POST /cities
    public void createUser() {

    }

    // DELETE /cities/:id
    public void deleteUser(int id) {

    }
}
