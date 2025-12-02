package org.senai.exception.mappers;

import org.senai.dtos.ErrorResponse;
import org.senai.exception.exceptions.BusinessRuleException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BusinessRuleExceptionMapper implements ExceptionMapper<BusinessRuleException> {
    @Override
    public Response toResponse(BusinessRuleException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage(), "BUSINESS_RULE_VIOLATION"))
                .build();
    }
}
