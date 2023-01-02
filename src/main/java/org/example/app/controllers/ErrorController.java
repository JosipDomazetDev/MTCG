package org.example.app.controllers;

import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;

public class ErrorController  extends Controller{
    public Response sendUnauthorized(Request request) {
        return new Response(
                HttpStatus.UNAUTHORIZED,
                ContentType.JSON,
                "{ \"error\": \"Correct token required\", \"data\": null }"
        );
    }
}
