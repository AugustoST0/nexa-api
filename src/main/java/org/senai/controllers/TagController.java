package org.senai.controllers;

import org.senai.dtos.TagCreateDTO;
import org.senai.dtos.TagUpdateDTO;
import org.senai.model.Tag;
import org.senai.services.TagService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TagController {

    @Inject
    TagService tagService;

    @GET
    public Response getAll() {
        List<Tag> tags = tagService.getAll();
        return Response.ok(tags).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Tag tag = tagService.getById(id);
        return Response.ok(tag).build();
    }

    @GET
    @Path("/grupo/{id}")
    public Response getTagsByGrupoId(@PathParam("id") Long id) {
        List<Tag> tags = tagService.getTagsByGrupoId(id);
        return Response.ok(tags).build();
    }

    @GET
    @Path("/not-in-grupo/{grupoId}")
    public Response getTagsNotInGrupo(@PathParam("grupoId") Long grupoId) {
        List<Tag> tags = tagService.getTagsNotInGrupo(grupoId);
        return Response.ok(tags).build();
    }

    @POST
    @RolesAllowed("USER")
    public Response create(@Valid TagCreateDTO dto) {
        Tag createdTag = tagService.create(dto);
        return Response.status(Response.Status.CREATED).entity(createdTag).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response update(@PathParam("id") Long id, @Valid TagUpdateDTO dto) {
        Tag tag = tagService.update(id, dto);
        return Response.ok(tag).build();
    }

    @POST
    @Path("/{tagId}/grupo/{grupoId}")
    @RolesAllowed("USER")
    public Response linkToGrupo(@PathParam("tagId") Long tagId, @PathParam("grupoId") Long grupoId) {
        tagService.linkToGrupo(tagId, grupoId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{tagId}/grupo/{grupoId}")
    @RolesAllowed("USER")
    public Response unlinkFromGrupo(@PathParam("tagId") Long tagId, @PathParam("grupoId") Long grupoId) {
        tagService.unlinkFromGrupo(tagId, grupoId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response delete(@PathParam("id") Long id) {
        tagService.delete(id);
        return Response.noContent().build();
    }
}