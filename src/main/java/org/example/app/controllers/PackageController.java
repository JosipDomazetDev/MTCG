package org.example.app.controllers;

import org.example.app.models.User;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;

public class PackageController extends Controller{
    public Response createPackage(Request request, User authenticatedUser) {
        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                "{ \"data\": , \"error\": null }"
        );
    }
}