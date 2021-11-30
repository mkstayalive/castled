package io.castled.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.apps.ExternalAppService;
import io.castled.oauth.OAuthDetails;
import io.castled.oauth.OAuthServiceType;
import io.castled.services.OAuthService;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/oauth")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@Slf4j
public class OAuthResource {

    private final ExternalAppService externalAppService;
    public OAuthService oAuthService;

    @Inject
    public OAuthResource(OAuthService oAuthService, ExternalAppService externalAppService) {
        this.oAuthService = oAuthService;
        this.externalAppService = externalAppService;
    }

    @GET
    @Path(("tokens/{id}"))
    public OAuthDetails getOAuthDetails(@PathParam("id") Long oAuthId) {
        return this.oAuthService.getOAuthDetails(oAuthId);
    }

    @GET
    @Path(("apps/{serviceType}/callback"))
    public Response handleAppCreationCallback(@PathParam("serviceType") OAuthServiceType oAuthServiceType,
                                              @QueryParam("state") String state, @QueryParam("code") String code) {
        return Response.seeOther(this.externalAppService.handleAuthorizationCallback(oAuthServiceType, state, code)).build();

    }
}
