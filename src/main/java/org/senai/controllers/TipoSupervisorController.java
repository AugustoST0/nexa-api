package org.senai.controllers;

import org.senai.model.TipoSupervisor;
import org.senai.services.TipoSupervisorService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/tipos-supervisor")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TipoSupervisorController {

    @Inject
    TipoSupervisorService tipoSupervisorService;

    @GET
    public Response getAll() {
        List<TipoSupervisor> tipos = tipoSupervisorService.getAll();
        return Response.ok(tipos).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        TipoSupervisor tipo = tipoSupervisorService.getById(id);
        return Response.ok(tipo).build();
    }

    @POST
    @RolesAllowed("USER")
    public Response create(@Valid TipoSupervisor tipoSupervisor) {
        TipoSupervisor createdTipo = tipoSupervisorService.create(tipoSupervisor);
        return Response.status(Response.Status.CREATED).entity(createdTipo).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response update(@PathParam("id") Long id, @Valid TipoSupervisor updatedTipo) {
        TipoSupervisor tipo = tipoSupervisorService.update(id, updatedTipo);
        return Response.ok(tipo).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response delete(@PathParam("id") Long id) {
        tipoSupervisorService.delete(id);
        return Response.noContent().build();
    }
}