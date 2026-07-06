package org.senai.controllers;

import org.senai.dtos.AtribuicaoTagMassaDTO;
import org.senai.dtos.AtribuirSupervisorDTO;
import org.senai.dtos.ColaboradorResponseDTO;
import org.senai.dtos.ColaboradorCreateUpdateDTO;
import org.senai.dtos.AdvancedSearchDTO;
import org.senai.dtos.ErrorResponse;
import org.senai.dtos.ImpactoExclusaoDTO;
import org.senai.exception.exceptions.BusinessRuleException;
import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.Colaborador;
import org.senai.services.ColaboradorService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/colaboradores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ColaboradorController {

    @Inject
    ColaboradorService colaboradorService;

    @GET
    public Response getAll() {
        List<Colaborador> colaboradores = colaboradorService.getAll();
        return Response.ok(colaboradores).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Colaborador colaborador = colaboradorService.getById(id);
        return Response.ok(colaborador).build();
    }

    @GET
    @Path("/matricula/{matricula}")
    public Response getByMatricula(@PathParam("matricula") String matricula) {
        Colaborador colaborador = colaboradorService.getByMatricula(matricula);
        return Response.ok(colaborador).build();
    }

    @GET
    @Path("/email/{email}")
    public Response getByEmail(@PathParam("email") String email) {
        Colaborador colaborador = colaboradorService.getByEmail(email);
        return Response.ok(colaborador).build();
    }

    @GET
    @Path("/cpf/{cpf}")
    public Response getByCpf(@PathParam("cpf") String cpf) {
        Colaborador colaborador = colaboradorService.getByCpf(cpf);
        return Response.ok(colaborador).build();
    }

    @GET
    @Path("/supervisores")
    public Response getSupervisores() {
        try {
            List<Colaborador> supervisores = colaboradorService.getSupervisores();
            return Response.ok(supervisores).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor ao buscar supervisores");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @GET
    @Path("/tag/{tagId}")
    public Response getByTag(@PathParam("tagId") Long tagId) {
        try {
            if (tagId == null || tagId <= 0) {
                ErrorResponse error = new ErrorResponse("INVALID_PARAMETERS", "ID da tag deve ser um número positivo");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }
            
            List<Colaborador> colaboradores = colaboradorService.getByTag(tagId);
            return Response.ok(colaboradores).build();
        } catch (RegisterNotFoundException e) {
            ErrorResponse error = new ErrorResponse("NOT_FOUND", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor ao buscar por tag");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @GET
    @Path("/search")
    public Response searchByNome(@QueryParam("nome") String nome) {
        try {
            if (nome == null || nome.trim().isEmpty()) {
                ErrorResponse error = new ErrorResponse("INVALID_PARAMETERS", "Nome é obrigatório para busca");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }
            
            if (nome.length() > 255) {
                ErrorResponse error = new ErrorResponse("INVALID_PARAMETERS", "Nome deve ter no máximo 255 caracteres");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }
            
            List<Colaborador> colaboradores = colaboradorService.searchByNome(nome);
            return Response.ok(colaboradores).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor ao buscar por nome");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @GET
    @Path("/search/common")
    public Response searchCommon(
            @QueryParam("nome") String nome,
            @QueryParam("matricula") String matricula,
            @QueryParam("email") String email,
            @QueryParam("cpf") String cpf,
            @QueryParam("cargo") String cargo) {
        try {
            var resultado = colaboradorService.searchCommon(nome, matricula, email, cpf, cargo);
            return Response.ok(resultado).build();
        } catch (BusinessRuleException e) {
            ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse("INVALID_PARAMETERS", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor ao realizar busca");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @POST
    @Path("/search/advanced")
    public Response searchAdvanced(AdvancedSearchDTO dto) {
        try {
            if (dto == null) {
                ErrorResponse error = new ErrorResponse("INVALID_REQUEST", "Dados de busca avançada são obrigatórios");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }

            var resultado = colaboradorService.searchAdvanced(
                    dto.tokens(), dto.supervisorIds(), dto.dataAdmissaoInicio(), dto.dataAdmissaoFim());
            return Response.ok(resultado).build();
        } catch (BusinessRuleException e) {
            ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse("INVALID_PARAMETERS", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor ao realizar busca avançada");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @POST
    @RolesAllowed("USER")
    public Response create(@Valid ColaboradorCreateUpdateDTO dto) {
        Colaborador colaborador = colaboradorService.create(dto);
        ColaboradorResponseDTO response = ColaboradorResponseDTO.fromEntity(colaborador);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response update(@PathParam("id") Long id, @Valid ColaboradorCreateUpdateDTO dto) {
        Colaborador colaborador = colaboradorService.update(id, dto);
        ColaboradorResponseDTO response = ColaboradorResponseDTO.fromEntity(colaborador);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{colaboradorId}/tag/{tagId}")
    @RolesAllowed("USER")
    public Response linkToTag(@PathParam("colaboradorId") Long colaboradorId, @PathParam("tagId") Long tagId) {
        colaboradorService.linkToTag(colaboradorId, tagId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{colaboradorId}/tag/{tagId}")
    @RolesAllowed("USER")
    public Response unlinkFromTag(@PathParam("colaboradorId") Long colaboradorId, @PathParam("tagId") Long tagId) {
        colaboradorService.unlinkFromTag(colaboradorId, tagId);
        return Response.noContent().build();
    }

    @POST
    @Path("/tags/atribuir-massa")
    @RolesAllowed("USER")
    public Response atribuirTagEmMassa(AtribuicaoTagMassaDTO dto) {
        colaboradorService.linkTagsEmMassa(dto.tagId(), dto.colaboradorIds());
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/supervisor")
    @RolesAllowed("USER")
    public Response atribuirSupervisor(@PathParam("id") Long id, @Valid AtribuirSupervisorDTO dto) {
        colaboradorService.atribuirSupervisor(id, dto);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/impacto")
    public Response getImpactoExclusao(@PathParam("id") Long id) {
        ImpactoExclusaoDTO impacto = colaboradorService.getImpactoExclusao(id);
        return Response.ok(impacto).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("USER")
    public Response delete(@PathParam("id") Long id) {
        colaboradorService.delete(id);
        return Response.noContent().build();
    }
}