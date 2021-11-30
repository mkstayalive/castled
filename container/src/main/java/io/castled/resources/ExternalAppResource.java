package io.castled.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.apps.ExternalApp;
import io.castled.apps.ExternalAppService;
import io.castled.apps.ExternalAppType;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.dtomappers.ExternalAppDTOMapper;
import io.castled.dtos.*;
import io.castled.forms.dtos.FieldOptionsDTO;
import io.castled.forms.dtos.FormFieldsDTO;
import io.castled.models.AppAggregate;
import io.castled.models.users.User;
import io.castled.resources.validators.ResourceAccessController;
import io.castled.services.PipelineService;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/v1/apps")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@Slf4j
@SuppressWarnings("rawtypes")
public class ExternalAppResource {

    private final ExternalAppService externalAppService;
    private final ResourceAccessController accessController;
    private final PipelineService pipelineService;

    @Inject
    public ExternalAppResource(ExternalAppService externalAppService, ResourceAccessController accessController,
                               PipelineService pipelineService) {
        this.externalAppService = externalAppService;
        this.accessController = accessController;
        this.pipelineService = pipelineService;
    }

    @POST
    public EntityCreateResponse createExternalApp(ExternalAppAttributes appAttributes,
                                                  @Auth User user) {
        return new EntityCreateResponse(this.externalAppService.createExternalApp(appAttributes.getName(), appAttributes.getConfig(), user));
    }

    @POST
    @Path("/oauth")
    public OauthAppCreateResponse createOauthExternalApp(OAuthAppAttributes appAttributes,
                                                         @Auth User user) {
        return new OauthAppCreateResponse(this.externalAppService.createOAuthExternalApp(user, appAttributes).toString());
    }

    @PUT
    @Path("/oauth/{id}")
    public OauthAppCreateResponse updateOauthExternalApp(@PathParam("id") Long appId, OAuthAppAttributes appAttributes,
                                                         @Auth User user) {
        return new OauthAppCreateResponse(this.externalAppService.updateOAuthExternalApp(user, appId, appAttributes).toString());
    }

    @PUT
    @Path("/{id}")
    public void updateExternalApp(@PathParam("id") Long appId, @Valid ExternalAppAttributes appAttributes,
                                  @Auth User user) {
        this.externalAppService.updateExternalApp(appId, user.getTeamId(), appAttributes.getName(),
                appAttributes.getConfig());
    }

    @DELETE
    @Path("/{id}")
    public void deleteExternalApp(@PathParam("id") Long appId,
                                  @Auth User user) {
        this.externalAppService.deleteExternalApp(appId, user.getTeamId());
    }

    @GET
    @Path("{id}")
    public ExternalAppDTO getExternalApp(@PathParam("id") Long appId,
                                         @Auth User user) {
        ExternalApp externalApp = this.externalAppService.getExternalApp(appId);
        this.accessController.validAppAccess(externalApp, user.getTeamId());
        return ExternalAppDTOMapper.INSTANCE.toDTO(externalApp,
                this.pipelineService.getAppPipelines(appId));
    }

    @POST
    @Path("/app-sync-options")
    public FieldOptionsDTO getAppSyncConfigOptions(@QueryParam("optionsRef") String optionsReference,
                                                   AppSyncConfigDTO appSyncConfigDTO) {
        return this.externalAppService.getAppSyncOptions(appSyncConfigDTO, optionsReference);
    }

    @GET
    @Path("/mapping-form-fields")
    public FormFieldsDTO getMappingFormFields(@NotNull @QueryParam("type") ExternalAppType appType) {
        return this.externalAppService.getMappingFormFields(appType);
    }

    @GET
    @Path("/form-fields")
    public FormFieldsDTO getFormFields(@NotNull @QueryParam("type") ExternalAppType appType) {
        return this.externalAppService.getFormFields(appType);
    }

    @GET
    public List<ExternalAppDTO> listExternalApps(@QueryParam("type") ExternalAppType externalAppType,
                                                 @Auth User user) {
        List<ExternalApp> externalApps = this.externalAppService.listExternalApps(user.getTeamId(), externalAppType);
        List<AppAggregate> appAggregates = this.pipelineService.getAppAggregates(user.getTeamId());
        return externalApps.stream().map(externalApp -> ExternalAppDTOMapper.INSTANCE.toDTO(externalApp, appAggregates))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/types")
    public List<ExternalAppTypeDTO> getExternalAppTypes(@Auth User user) {
        return this.externalAppService.listExternalAppTypes(user);
    }

}
