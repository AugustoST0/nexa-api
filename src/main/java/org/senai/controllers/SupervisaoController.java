package org.senai.controllers;

import org.senai.dtos.MigrarSupervisionadosDTO;
import org.senai.dtos.TrocarSupervisorDTO;
import org.senai.model.Supervisao;
import org.senai.services.SupervisaoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/supervisoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SupervisaoController {

    @Inject
    SupervisaoService supervisaoService;

    @GET
    public Response getAll() {
        List<Supervisao> supervisoes = supervisaoService.getAll();
        return Response.ok(supervisoes).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Supervisao supervisao = supervisaoService.getById(id);
        return Response.ok(supervisao).build();
    }

    @GET
    @Path("/supervisor/{supervisorId}")
    public Response getSupervisionadosPorSupervisor(@PathParam("supervisorId") Long supervisorId) {
        List<Supervisao> supervisoes = supervisaoService.getSupervisionadosPorSupervisor(supervisorId);
        return Response.ok(supervisoes).build();
    }

    @GET
    @Path("/colaborador/{colaboradorId}")
    public Response getSupervisoresPorColaborador(@PathParam("colaboradorId") Long colaboradorId) {
        List<Supervisao> supervisoes = supervisaoService.getSupervisoresPorColaborador(colaboradorId);
        return Response.ok(supervisoes).build();
    }

    @GET
    @Path("/supervisor/{supervisorId}/tipo/{tipoId}")
    public Response getSupervisionadosPorSupervisorETipo(
            @PathParam("supervisorId") Long supervisorId,
            @PathParam("tipoId") Long tipoId) {
        List<Supervisao> supervisoes = supervisaoService.getSupervisionadosPorSupervisorETipo(supervisorId, tipoId);
        return Response.ok(supervisoes).build();
    }

    @GET
    @Path("/tipo/{tipoId}")
    public Response getSupervisionadosPorTipo(@PathParam("tipoId") Long tipoId) {
        List<Supervisao> supervisoes = supervisaoService.getSupervisionadosPorTipo(tipoId);
        return Response.ok(supervisoes).build();
    }

    @GET
    @Path("/historico/supervisionado/{colaboradorId}")
    public Response getHistoricoSupervisionado(@PathParam("colaboradorId") Long colaboradorId) {
        List<Supervisao> supervisoes = supervisaoService.getHistoricoSupervisionado(colaboradorId);
        return Response.ok(supervisoes).build();
    }

    @GET
    @Path("/historico/supervisor/{supervisorId}")
    public Response getHistoricoSupervisor(@PathParam("supervisorId") Long supervisorId) {
        List<Supervisao> supervisoes = supervisaoService.getHistoricoSupervisor(supervisorId);
        return Response.ok(supervisoes).build();
    }

    @POST
    @RolesAllowed("USER")
    public Response create(@Valid Supervisao supervisao) {
        Supervisao createdSupervisao = supervisaoService.create(supervisao);
        return Response.status(Response.Status.CREATED).entity(createdSupervisao).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response update(@PathParam("id") Long id, @Valid Supervisao updatedSupervisao) {
        Supervisao supervisao = supervisaoService.update(id, updatedSupervisao);
        return Response.ok(supervisao).build();
    }

    @PUT
    @Path("/{id}/encerrar")
    @RolesAllowed("USER")
    public Response encerrarSupervisao(@PathParam("id") Long id) {
        supervisaoService.encerrarSupervisao(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/trocar-supervisor")
    @RolesAllowed("USER")
    public Response trocarSupervisor(@Valid TrocarSupervisorDTO dto) {
        supervisaoService.trocarSupervisor(dto);
        return Response.noContent().build();
    }

    @POST
    @Path("/migrar-todos")
    @RolesAllowed("USER")
    public Response migrarTodosSupervisionados(@Valid MigrarSupervisionadosDTO dto) {
        supervisaoService.migrarTodosSupervisionados(dto);
        return Response.noContent().build();
    }

    @POST
    @Path("/migrar-seletivo")
    @RolesAllowed("USER")
    public Response migrarSupervisionadosSeletivo(@Valid MigrarSupervisionadosDTO dto) {
        supervisaoService.migrarSupervisionadosSeletivo(dto);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response delete(@PathParam("id") Long id) {
        supervisaoService.delete(id);
        return Response.noContent().build();
    }
}