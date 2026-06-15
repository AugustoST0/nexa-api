package org.senai.controllers;

import org.senai.model.Relatorio;
import org.senai.services.RelatorioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/relatorios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class RelatorioController {

    @Inject
    RelatorioService relatorioService;

    @POST
    @Path("/gerar/{grupoId}")
    @RolesAllowed("USER")
    public Response gerar(@PathParam("grupoId") Long grupoId) {
        return Response.status(Response.Status.CREATED)
                .entity(relatorioService.gerar(grupoId))
                .build();
    }

    @GET
    public Response getAll() {
        return Response.ok(relatorioService.getAll()).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Relatorio relatorio = relatorioService.getById(id);
        return Response.ok(relatorio).build();
    }

    @GET
    @Path("/{id}/csv")
    @Produces("text/csv")
    public Response downloadCsv(@PathParam("id") Long id) {
        String csv = relatorioService.downloadCsv(id);
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"relatorio-" + id + ".csv\"")
                .build();
    }

    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    public Response downloadPdf(@PathParam("id") Long id) {
        byte[] pdf = relatorioService.downloadPdf(id);
        return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=\"relatorio-" + id + ".pdf\"")
                .build();
    }
}
