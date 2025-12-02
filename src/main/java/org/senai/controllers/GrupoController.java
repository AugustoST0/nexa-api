package org.senai.controllers;

import org.senai.model.Grupo;
import org.senai.model.Tag;
import org.senai.services.GrupoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/grupos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class GrupoController {

    @Inject
    GrupoService grupoService;

    @GET
    public Response getAll() {
        List<Grupo> grupos = grupoService.getAll();
        return Response.ok(grupos).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Grupo grupo = grupoService.getById(id);
        return Response.ok(grupo).build();
    }

    @POST
    @RolesAllowed("USER")
    public Response create(@Valid Grupo grupo) {
        Grupo createdGrupo = grupoService.create(grupo);
        return Response.status(Response.Status.CREATED).entity(createdGrupo).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response update(@PathParam("id") Long id, @Valid Grupo updatedGrupo) {
        Grupo grupo = grupoService.update(id, updatedGrupo);
        return Response.ok(grupo).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response delete(@PathParam("id") Long id) {
        grupoService.delete(id);
        return Response.noContent().build();
    }
}
