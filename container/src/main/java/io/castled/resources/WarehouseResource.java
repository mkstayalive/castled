package io.castled.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.dtomappers.WarehouseDTOMapper;
import io.castled.dtos.ConfigUpdateRequest;
import io.castled.dtos.EntityCreateResponse;
import io.castled.dtos.WarehouseDTO;
import io.castled.dtos.WarehouseQueryPreviewRequest;
import io.castled.exceptions.CastledException;
import io.castled.forms.dtos.FormFieldsDTO;
import io.castled.models.QueryId;
import io.castled.models.QueryStatusAndResults;
import io.castled.models.Warehouse;
import io.castled.models.WarehouseAggregate;
import io.castled.models.users.User;
import io.castled.resources.validators.ResourceAccessController;
import io.castled.services.PipelineService;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseService;
import io.castled.warehouses.WarehouseType;
import io.castled.warehouses.dtos.WarehouseAttributes;
import io.castled.warehouses.dtos.WarehouseTypeDTO;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/v1/warehouses")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@Slf4j
public class WarehouseResource {

    private final WarehouseService warehouseService;
    private final PipelineService pipelineService;
    private final ResourceAccessController resourceAccessController;

    @Inject
    public WarehouseResource(WarehouseService warehouseService, ResourceAccessController resourceAccessController,
                             PipelineService pipelineService) {
        this.warehouseService = warehouseService;
        this.resourceAccessController = resourceAccessController;
        this.pipelineService = pipelineService;
    }

    @POST
    public EntityCreateResponse createWarehouse(@Valid WarehouseAttributes warehouseAttributes,
                                                @Auth User user) {
        return new EntityCreateResponse(this.warehouseService.createWarehouse(warehouseAttributes, user));
    }

    @GET
    @Path("/form-fields")
    public FormFieldsDTO getFormFields(@QueryParam("type") WarehouseType warehouseType) {
        return this.warehouseService.getFormFields(warehouseType);
    }

    @PUT
    @Path("/{id}")
    public void updateWarehouse(@PathParam("id") Long warehouseId, WarehouseAttributes warehouseAttributes
            , @Auth User user) throws Exception {
        this.warehouseService.updateWarehouse(warehouseId, warehouseAttributes, user);
    }

    @GET
    @Path("/{id}")
    public WarehouseDTO getWarehouse(@PathParam("id") Long warehouseId,
                                     @Auth User user) {
        Warehouse warehouse = this.warehouseService.getWarehouse(warehouseId);
        this.resourceAccessController.validateWarehouseAccess(warehouse, user.getTeamId());
        return WarehouseDTOMapper.INSTANCE.toDTO(warehouse, this.pipelineService.getWarehousePipelines(warehouseId));
    }

    @DELETE
    @Path("/{id}")
    public void deleteWarehouse(@PathParam("id") Long warehouseId,
                                @Auth User user) {
        warehouseService.deleteWarehouse(warehouseId, user.getTeamId());
    }

    @GET
    public List<WarehouseDTO> getAllWarehouses(@QueryParam("type") WarehouseType warehouseType,
                                               @Auth User user) {
        List<Warehouse> warehouses = this.warehouseService.getAllWarehouses(warehouseType, user.getTeamId());
        List<WarehouseAggregate> warehouseAggregates = this.pipelineService.getWarehouseAggregates(user.getTeamId());
        return warehouses.stream().map(warehouse -> WarehouseDTOMapper.INSTANCE.toDTO(warehouse, warehouseAggregates))
                .collect(Collectors.toList());
    }

    @PUT
    @Path("/{id}/config")
    public void updateWarehouseConfig(@PathParam("id") Long warehouseId, ConfigUpdateRequest configUpdateRequest,
                                      @Auth User user) {
        this.warehouseService.updateWarehouseConfig(warehouseId, configUpdateRequest.getConfig(), user.getTeamId());
    }

    @Path("/test")
    @POST
    public void testConnection(WarehouseConfig warehouseConfig) throws CastledException {
        this.warehouseService.testConnection(warehouseConfig.getType(), warehouseConfig);
    }


    @PUT
    @Path("/{id}/query-preview")
    public QueryId executePreviewQuery(@PathParam("id") Long warehouseId,
                                       WarehouseQueryPreviewRequest warehouseQueryPreviewRequest,
                                       @Auth User user) throws Exception {
        return this.warehouseService.previewQuery(warehouseId, warehouseQueryPreviewRequest.getQuery(), user.getTeamId());
    }

    @GET
    @Path(("/queries/{query_id}/results"))
    public QueryStatusAndResults getQueryResults(@PathParam("query_id") String queryId) throws Exception {
        return this.warehouseService.getQueryResults(queryId);
    }

    @GET
    @Path("/types")
    public List<WarehouseTypeDTO> getWarehouseTypes(@Auth User user) throws Exception {
        return this.warehouseService.listWarehouseTypes(user);
    }
}
