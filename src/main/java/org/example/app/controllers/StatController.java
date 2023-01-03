package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Stat;
import org.example.app.models.User;
import org.example.app.services.UserService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;

import java.util.List;

public class StatController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private UserService userService;

    public StatController( UserService userService) {
        setUserService(userService);
    }

    public Response getStats(User authenticatedUser) throws JsonProcessingException {
        Stat stat = userService.getStats(authenticatedUser);

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                getObjectMapper().writeValueAsString(stat)
        );
    }

    public Response getScores() throws JsonProcessingException {
        List<Stat> stats = userService.getScoreboard();

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                getObjectMapper().writeValueAsString(stats)
        );
    }
}
