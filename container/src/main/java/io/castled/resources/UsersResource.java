package io.castled.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.dtos.UserDTO;
import io.castled.models.users.User;
import io.castled.services.UsersService;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@Slf4j
public class UsersResource {
    private final UsersService usersService;

    @Inject
    public UsersResource(UsersService usersService) {
        this.usersService = usersService;
    }

    @GET
    @Path("whoami")
    public UserDTO getLoggedInUser(@Auth User user) {
        return usersService.toDTO(user);
    }
}
