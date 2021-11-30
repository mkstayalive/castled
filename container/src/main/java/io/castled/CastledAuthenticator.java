package io.castled;

import com.google.inject.Inject;
import io.castled.models.users.User;
import io.castled.services.UsersService;
import io.dropwizard.auth.Authenticator;

import java.util.Optional;

public class CastledAuthenticator implements Authenticator<String, User> {

    private final UsersService usersService;

    @Inject
    public CastledAuthenticator(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public Optional<User> authenticate(String dummy) {
        return Optional.ofNullable(usersService.getUser());
    }
}