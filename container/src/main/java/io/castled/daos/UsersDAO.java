package io.castled.daos;

import io.castled.constants.TableFields;
import io.castled.models.users.TeamAndUser;
import io.castled.models.users.User;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RegisterRowMapper(UsersDAO.UsersRowMapper.class)
public interface UsersDAO {

    @SqlUpdate("insert into users(team_id, email, first_name, last_name)" +
            " values(:teamId, :email, :firstName, :lastName)")
    @GetGeneratedKeys
    long createUser(@Bind("teamId") Long teamid, @Bind("email") String email, @Bind("firstName") String firstName, @Bind("lastName") String lastName);

    @SqlQuery("select * from users where id = :id and is_deleted = 0")
    User getUser(@Bind("id") Long userId);

    @SqlQuery("select * from users where is_deleted = 0")
    List<User> getAllUsers();

    @CreateSqlObject
    TeamsDAO createTeamsDAO();


    @Transaction
    default TeamAndUser createTeamAndUser(String teamName, String email, String firstName, String lastName) {
        long teamId = createTeamsDAO().createTeam(teamName);
        return new TeamAndUser(teamId, createUser(teamId, email, firstName, lastName));
    }

    class UsersRowMapper implements RowMapper<User> {

        @Override
        public User map(ResultSet rs, StatementContext ctx) throws SQLException {
            Long teamId = rs.getLong(TableFields.TEAM_ID);
            Timestamp createdTs = rs.getTimestamp("created_ts");
            return User.builder()
                    .id(rs.getLong(TableFields.ID))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .teamId(teamId).isDeleted(rs.getBoolean(TableFields.IS_DELETED))
                    .createdTs(createdTs.getTime())
                    .build();
        }
    }
}
