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

public class SessionController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private UserService userService;

    public SessionController(UserService userService) {
        setUserService(userService);
    }

    public Response login(Request request) throws JsonProcessingException {
        User proposedUser = getObjectMapper().readValue(request.getBody(), User.class);
        boolean loginSuccessful = getUserService().login(proposedUser);

        if (!loginSuccessful) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{\"error\": \"Login failed.\"}"
            );
        }

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                getObjectMapper().writeValueAsString(proposedUser.getToken())
        );
    }

    public User getAuthenticatedUser(String token) {
        if (token == null) return null;

        return userService.getAuthenticatedUser(token);
    }
}
