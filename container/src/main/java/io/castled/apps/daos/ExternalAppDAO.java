package io.castled.apps.daos;

import io.castled.ObjectRegistry;
import io.castled.apps.AppConfig;
import io.castled.apps.ExternalApp;
import io.castled.apps.ExternalAppStatus;
import io.castled.apps.ExternalAppType;
import io.castled.constants.TableFields;
import io.castled.encryption.EncryptionException;
import io.castled.encryption.EncryptionManager;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RegisterRowMapper(ExternalAppDAO.ExternalAppRowMapper.class)
public interface ExternalAppDAO {

    @GetGeneratedKeys
    @SqlUpdate("insert into external_apps(name, config, type, team_id, status) values(:name, :config, :type, :teamId, 'OK')")
    long createExternalApp(@Bind("name") String name, @Bind("type") ExternalAppType type,
                           @Bind("config") String config, @Bind("teamId") Long teamId);

    @SqlQuery("select * from external_apps where id = :id and is_deleted = 0")
    ExternalApp getExternalApp(@Bind("id") Long id);

    @SqlUpdate("update external_apps set is_deleted = 1 where id = :id")
    void deleteApp(@Bind("id") Long id);

    @SqlUpdate("update external_apps set name = :name, config = :config where id = :id")
    void updateExternalApp(@Bind("id") Long id, @Bind("name") String name, @Bind("config") String config);

    @SqlQuery("select * from external_apps where team_id = :teamId and is_deleted =0 order by id desc")
    List<ExternalApp> listExternalApps(@Bind("teamId") Long teamId);

    @Slf4j
    class ExternalAppRowMapper implements RowMapper<ExternalApp> {

        @Override
        public ExternalApp map(ResultSet rs, StatementContext ctx) throws SQLException {

            ExternalAppType externalAppType = ExternalAppType.valueOf(rs.getString(TableFields.TYPE));
            try {
                EncryptionManager encryptionManager = ObjectRegistry.getInstance(EncryptionManager.class);
                Long teamId = rs.getLong(TableFields.TEAM_ID);
                String encryptedConfig = rs.getString(TableFields.CONFIG);
                String configString = encryptionManager.decryptText(encryptedConfig, teamId);
                return ExternalApp.builder().name(rs.getString(TableFields.NAME)).id(rs.getLong(TableFields.ID))
                        .config(JsonUtils.jsonStringToObject(configString, AppConfig.class))
                        .status(ExternalAppStatus.valueOf(rs.getString(TableFields.STATUS)))
                        .type(externalAppType).teamId(teamId)
                        .build();
            } catch (EncryptionException e) {
                log.error("External app row mapper failed", e);
                throw new CastledRuntimeException(e);
            }
        }
    }

}
