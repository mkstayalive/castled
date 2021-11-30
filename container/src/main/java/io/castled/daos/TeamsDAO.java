package io.castled.daos;

import io.castled.constants.TableFields;
import io.castled.encryption.EncryptionUtils;
import io.castled.encryption.TeamEncryptionKey;
import io.castled.models.Team;
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

@RegisterRowMapper(TeamsDAO.TeamsMapper.class)
public interface TeamsDAO {

    @SqlUpdate("insert into teams(name) values(:teamName)")
    @GetGeneratedKeys
    long doCreateTeam(@Bind("teamName") String teamName);

    @CreateSqlObject
    EncryptionKeysDAO createEncryptionKeysDAO();

    @SqlQuery("select * from teams where id = :teamId")
    Team getTeam(@Bind("teamId") Long teamId);

    @SqlQuery("select * from teams where name = :teamName")
    Team getTeamByName(@Bind("teamName") String teamName);

    class TeamsMapper implements RowMapper<Team> {

        @Override
        public Team map(ResultSet rs, StatementContext ctx) throws SQLException {
            return Team.builder().id(rs.getLong(TableFields.ID))
                    .name(rs.getString(TableFields.NAME)).build();
        }
    }

    @Transaction
    default Long createTeam(String teamName) {
        Long teamId = doCreateTeam(teamName);
        createEncryptionKeysDAO().createEncryptionKey(TeamEncryptionKey.builder().teamId(teamId)
                .cipherKey(EncryptionUtils.generateEncryptionKey(32)).build());
        return teamId;
    }
}
