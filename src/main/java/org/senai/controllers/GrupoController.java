package org.senai.controllers;

import org.senai.dtos.DesativarGrupoDTO;
import org.senai.dtos.GrupoCreateDTO;
import org.senai.model.Grupo;
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
    public Response create(@Valid GrupoCreateDTO dto) {
        Grupo createdGrupo = grupoService.create(dto);
        return Response.status(Response.Status.CREATED).entity(createdGrupo).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response update(@PathParam("id") Long id, @Valid GrupoCreateDTO dto) {
        Grupo grupo = grupoService.update(id, dto);
        return Response.ok(grupo).build();
    }

    @PUT
    @Path("/{id}/desativar")
    @RolesAllowed("USER")
    public Response desativar(@PathParam("id") Long id, DesativarGrupoDTO dto) {
        Grupo grupo = grupoService.desativar(id, dto != null ? dto.motivo() : null);
        return Response.ok(grupo).build();
    }

    @PUT
    @Path("/{id}/ativar")
    @RolesAllowed("USER")
    public Response ativar(@PathParam("id") Long id) {
        Grupo grupo = grupoService.ativar(id);
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
