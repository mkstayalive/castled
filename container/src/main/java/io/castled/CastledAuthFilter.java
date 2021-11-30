package io.castled;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.models.users.User;
import io.dropwizard.auth.AuthFilter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import java.io.IOException;

@Singleton
public class CastledAuthFilter extends AuthFilter<String, User> {

    @Inject
    public CastledAuthFilter(CastledAuthenticator castledAuthenticator) {
        this.authenticator = castledAuthenticator;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        if (!authenticate(containerRequestContext, "dummy", null)) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }

    }
}
