package io.castled.resources.validators;

import com.google.inject.Singleton;
import io.castled.apps.ExternalApp;
import io.castled.models.Pipeline;
import io.castled.models.Warehouse;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

@Singleton
public class ResourceAccessController {

    public void validAppAccess(ExternalApp externalApp, long teamId) throws ForbiddenException {
        if (externalApp == null) {
            throw new NotFoundException("App not found");
        }
        if (!externalApp.getTeamId().equals(teamId)) {
            throw new ForbiddenException(String.format("User not authorized to access app %s", externalApp.getName()));
        }
    }

    public void validateWarehouseAccess(Warehouse warehouse, long teamId) throws ForbiddenException {
        if (warehouse == null) {
            throw new NotFoundException("Warehouse not found");
        }
        if (!warehouse.getTeamId().equals(teamId)) {
            throw new ForbiddenException(String.format("User not authorized to access warehouse %s", warehouse.getName()));
        }
    }

    public void validatePipelineAccess(Pipeline pipeline, long teamId) throws ForbiddenException {
        if (pipeline == null) {
            throw new NotFoundException("Pipeline not found");
        }
        if (!pipeline.getTeamId().equals(teamId)) {
            throw new ForbiddenException(String.format("User not authorized to access pipeline %s", pipeline.getName()));
        }
    }
}
