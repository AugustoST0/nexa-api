package org.senai.controllers;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.senai.dtos.ImportPreviewDTO;
import org.senai.dtos.LinhaImportDTO;
import org.senai.services.ImportacaoColaboradorService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Path("/colaboradores/importar")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ImportacaoController {

    @Inject
    ImportacaoColaboradorService importacaoService;

    @POST
    @Path("/preview")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed("USER")
    public Response preview(
            @RestForm("arquivo") FileUpload arquivo,
            @RestForm("delimitador") @DefaultValue(";") String delimitador) {
        try {
            if (arquivo == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("erro", "Arquivo CSV é obrigatório"))
                        .build();
            }

            char sep = (delimitador != null && !delimitador.isEmpty()) ? delimitador.charAt(0) : ';';

            try (InputStream is = Files.newInputStream(arquivo.uploadedFile())) {
                ImportPreviewDTO preview = importacaoService.parsePreview(is, sep);
                return Response.ok(preview).build();
            }
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("erro", "Erro ao processar arquivo: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("erro", "Erro interno ao processar CSV"))
                    .build();
        }
    }

    @POST
    @Path("/validar")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response validar(List<Map<String, String>> linhasMapeadas) {
        try {
            if (linhasMapeadas == null || linhasMapeadas.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("erro", "Lista de linhas é obrigatória"))
                        .build();
            }

            List<LinhaImportDTO> resultado = importacaoService.validarLinhas(linhasMapeadas);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("erro", "Erro interno ao validar linhas"))
                    .build();
        }
    }

    @POST
    @Path("/confirmar")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response confirmar(List<LinhaImportDTO> linhas) {
        try {
            if (linhas == null || linhas.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("erro", "Lista de linhas é obrigatória"))
                        .build();
            }

            Map<String, Object> resumo = importacaoService.importar(linhas);
            return Response.ok(resumo).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("erro", "Erro interno ao importar colaboradores"))
                    .build();
        }
    }
}
