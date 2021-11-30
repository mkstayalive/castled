package io.castled.resources;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.dtomappers.PipelineDTOMapper;
import io.castled.dtos.*;
import io.castled.models.Pipeline;
import io.castled.models.users.User;
import io.castled.resources.validators.ResourceAccessController;
import io.castled.services.PipelineService;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/v1/pipelines")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@Slf4j
public class PipelineResource {

    private final PipelineService pipelineService;
    private final ResourceAccessController resourceAccessController;

    @Inject
    public PipelineResource(PipelineService pipelineService, ResourceAccessController resourceAccessController) {
        this.pipelineService = pipelineService;
        this.resourceAccessController = resourceAccessController;
    }

    @POST
    public EntityCreateResponse createPipeline(@Valid PipelineConfigDTO pipelineConfigDTO,
                                               @Auth User user) {
        return new EntityCreateResponse(this.pipelineService.createPipeline(pipelineConfigDTO, user));
    }

    @PUT
    @Path("{id}/trigger-run")
    public void triggerPipeline(@PathParam("id") Long pipelineId,
                                @Auth User user) {
        this.pipelineService.triggerPipeline(pipelineId, user.getTeamId());
    }

    @PUT
    @Path("{id}/restart")
    public void restartPipeline(@PathParam("id") Long pipelineId,
                                @Auth User user) throws Exception {
        this.pipelineService.restartPipeline(pipelineId, user.getTeamId());
    }

    @PUT
    @Path("{id}")
    public void updatePipeline(@PathParam("id") Long pipelineId, PipelineUpdateRequest pipelineUpdateRequest) {
        this.pipelineService.updatePipeline(pipelineId, pipelineUpdateRequest);
    }

    @PUT
    @Path("{id}/pause")
    public void pausePipeline(@PathParam("id") Long pipelineId,
                              @Auth User user) throws Exception {
        this.pipelineService.pausePipeline(pipelineId, user.getTeamId());
    }

    @PUT
    @Path("{id}/resume")
    public void resumePipeline(@PathParam("id") Long pipelineId,
                               @Auth User user) throws Exception {
        this.pipelineService.resumePipeline(pipelineId, user.getTeamId());
    }

    @GET
    @Path("/{id}/short")
    public Pipeline getPipelineShort(@PathParam("id") Long id, @QueryParam("cached") boolean cached,
                                     @Auth User user) {
        Pipeline pipeline = this.pipelineService.getActivePipeline(id, cached);
        this.resourceAccessController.validatePipelineAccess(pipeline, user.getTeamId());
        return pipeline;
    }

    @GET
    @Path("/{id}")
    public PipelineDTO getPipeline(@PathParam("id") Long id,
                                   @Auth User user) {
        Pipeline pipeline = this.pipelineService.getActivePipeline(id);
        this.resourceAccessController.validatePipelineAccess(pipeline, user.getTeamId());
        return PipelineDTOMapper.INSTANCE.toDetailedDTO(pipeline);
    }

    @DELETE
    @Path("/{id}")
    public void deletePipeline(@PathParam("id") Long id,
                               @Auth User user) {
        this.pipelineService.deletePipeline(id, user.getTeamId());
    }

    @POST
    @Path("/schemas/fetch")
    public PipelineSchema getPipelineSchema(AppSyncConfigDTO appSyncConfig) throws Exception {
        return this.pipelineService.getPipelineSchema(appSyncConfig);
    }


    @GET
    public List<PipelineDTO> getAllPipelines(@QueryParam("appId") Long appId,
                                             @Auth User user) {
        return this.pipelineService.listPipelines(user.getTeamId(), appId).stream()
                .map(PipelineDTOMapper.INSTANCE::toDTO).collect(Collectors.toList());
    }

    @GET
    @Path("/dummy")
    public void triggerDummyJob() {
        this.pipelineService.triggerDummyRun();
    }
}
