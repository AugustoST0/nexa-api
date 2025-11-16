package org.senai.exception.mappers;

import org.senai.dtos.ErrorResponse;
import org.senai.exception.exceptions.InvalidCredentialsException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidCredentialsExceptionMapper implements ExceptionMapper<InvalidCredentialsException> {
    @Override
    public Response toResponse(InvalidCredentialsException e) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), "INVALID_CREDENTIALS"))
                .build();
    }
}