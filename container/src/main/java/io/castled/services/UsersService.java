package io.castled.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.daos.TeamsDAO;
import io.castled.daos.UsersDAO;
import io.castled.dtos.UserDTO;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.models.Team;
import io.castled.models.users.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

@Singleton
@Slf4j
public class UsersService {

    private final TeamsDAO teamsDAO;
    private final UsersDAO usersDAO;

    @Inject
    public UsersService(Jdbi jdbi) {
        this.teamsDAO = jdbi.onDemand(TeamsDAO.class);
        this.usersDAO = jdbi.onDemand(UsersDAO.class);
    }

    public UserDTO toDTO(User user) {
        Team team = this.teamsDAO.getTeam(user.getTeamId());
        return new UserDTO(user.getFullName(), user.getEmail(),
                user.getId(), user.getCreatedTs(), team);
    }

    public void createTestTeamAndUser() {
        this.usersDAO.createTeamAndUser("test", "test@castled.io", "Test", "User");
    }

    public User getUser() {
        List<User> users = this.usersDAO.getAllUsers();
        if (CollectionUtils.isEmpty(users)) {
            return null;
        }
        if (users.size() > 1) {
            throw new CastledRuntimeException("Multiple users found in database");
        }
        return users.get(0);
    }

}
