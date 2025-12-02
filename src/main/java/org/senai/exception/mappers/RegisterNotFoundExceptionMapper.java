package org.senai.exception.mappers;

import org.senai.dtos.ErrorResponse;
import org.senai.exception.exceptions.RegisterNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RegisterNotFoundExceptionMapper implements ExceptionMapper<RegisterNotFoundException> {
    @Override
    public Response toResponse(RegisterNotFoundException e) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), "USER_NOT_FOUND"))
                .build();
    }
}
