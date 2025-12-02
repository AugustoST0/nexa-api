package org.senai.exception.mappers;

import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.senai.dtos.ErrorResponse;

import java.sql.SQLIntegrityConstraintViolationException;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    @Override
    public Response toResponse(PersistenceException e) {

        if (e instanceof ConstraintViolationException cve) {
            String msg = cve.getMessage().toLowerCase();

            if (msg.contains("duplicate entry")) {
                if (msg.contains("email")) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(new ErrorResponse("Email já cadastrado", "EMAIL_ALREADY_EXISTS"))
                            .build();
                }
                if (msg.contains("cpf")) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(new ErrorResponse("CPF já cadastrado", "CPF_ALREADY_EXISTS"))
                            .build();
                }
                if (msg.contains("matricula")) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(new ErrorResponse("Matrícula já cadastrada", "MATRICULA_ALREADY_EXISTS"))
                            .build();
                }
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse("Registro já cadastrado", "DUPLICATE_ENTRY"))
                        .build();
            }
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Erro interno ao persistir dados", "INTERNAL_ERROR"))
                .build();
    }
}
