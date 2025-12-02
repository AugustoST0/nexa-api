package org.senai.exception.mappers;

import org.senai.dtos.ErrorResponse;
import org.senai.exception.exceptions.InvalidRefreshTokenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidRefreshTokenExceptionMapper implements ExceptionMapper<InvalidRefreshTokenException> {
    @Override
    public Response toResponse(InvalidRefreshTokenException e) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), "INVALID_REFRESH_TOKEN"))
                .build();
    }
}