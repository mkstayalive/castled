package io.castled.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.errors.PipelineErrorAndSample;
import io.castled.models.PipelineRun;
import io.castled.services.PipelineService;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/v1/pipeline-runs")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@Slf4j
public class PipelineRunResource {

    private final PipelineService pipelineService;

    @Inject
    public PipelineRunResource(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @GET
    @Path("{run_id}/errors")
    public List<PipelineErrorAndSample> getRunErrors(@PathParam("run_id") Long runId) {
        return this.pipelineService.getPipelineRunErrors(runId).getErrorAndSamples();
    }

    @GET
    @Path("{run_id}")
    public PipelineRun getPipelineRun(@PathParam("run_id") Long runId) {
        return this.pipelineService.getPipelineRun(runId);
    }

    @GET
    @Path("/pipelines/{pipeline_id}")
    public List<PipelineRun> getPipelineRuns(@PathParam("pipeline_id") Long pipelineId,
                                             @QueryParam("limit") int limit) {
        return this.pipelineService.getPipelineRuns(pipelineId, limit);
    }

    @GET
    @Path("{run_id}/download-errors")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadErrorReport(@PathParam("run_id") Long runId) throws Exception {
        return Response.ok(this.pipelineService.downloadErrorReport(runId), MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = error_report.csv")
                .build();
    }


}
