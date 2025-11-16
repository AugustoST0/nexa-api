package org.senai.controllers;

import org.senai.dtos.LoginRequestDTO;
import org.senai.dtos.RefreshTokenRequestDTO;
import org.senai.dtos.TokenResponseDTO;
import org.senai.services.AuthService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequestDTO dto) {
        TokenResponseDTO response = authService.authenticateUser(dto);
        return Response.ok(response).build();
    }

    @POST
    @Path("/refresh")
    public Response refreshToken(@Valid RefreshTokenRequestDTO dto) {
        TokenResponseDTO tokenResponse = authService.refreshTokens(dto.refreshToken());
        return Response.ok(tokenResponse).build();
    }
}