package org.senai.controllers;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.senai.dtos.CategoriaTagDTO;
import org.senai.model.CategoriaTag;
import org.senai.services.CategoriaTagService;

import java.util.List;

@Path("/categorias-tag")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CategoriaTagController {

    @Inject
    CategoriaTagService categoriaTagService;

    @GET
    public Response getAll() {
        List<CategoriaTag> categorias = categoriaTagService.getAll();
        return Response.ok(categorias).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        CategoriaTag categoria = categoriaTagService.getById(id);
        return Response.ok(categoria).build();
    }

    @POST
    @RolesAllowed("USER")
    public Response create(@Valid CategoriaTagDTO dto) {
        CategoriaTag categoria = categoriaTagService.create(dto);
        return Response.status(Response.Status.CREATED).entity(categoria).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response update(@PathParam("id") Long id, @Valid CategoriaTagDTO dto) {
        CategoriaTag categoria = categoriaTagService.update(id, dto);
        return Response.ok(categoria).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response delete(@PathParam("id") Long id) {
        categoriaTagService.delete(id);
        return Response.noContent().build();
    }
}
