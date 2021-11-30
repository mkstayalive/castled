package io.castled.warehouses;

import io.castled.ObjectRegistry;
import io.castled.constants.TableFields;
import io.castled.encryption.EncryptionException;
import io.castled.encryption.EncryptionManager;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.models.Warehouse;
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

@RegisterRowMapper(WarehouseDAO.WarehouseRowMapper.class)
public interface WarehouseDAO {

    @GetGeneratedKeys
    @SqlUpdate("insert into warehouses(name, config, type, team_id, status) values(:name, :config, :type, :teamId, 'OK')")
    long createWarehouse(@Bind("name") String name, @Bind("type") WarehouseType type,
                         @Bind("config") String config, @Bind("teamId") Long teamId);

    @SqlUpdate("update warehouses set name = :name, config = :config where id = :id")
    void updateWarehouse(@Bind("id") Long id, @Bind("name") String name, @Bind("config") String config);

    @SqlUpdate("update warehouses set is_deleted = 1 where id = :id")
    void deleteWarehouse(@Bind("id") Long warehouseId);

    @SqlQuery("select * from warehouses where id = :id")
    Warehouse getWarehouse(@Bind("id") Long id);

    @SqlUpdate("update warehouses set config =:config where id =:id")
    void updateWarehouseConfig(@Bind("id") Long id, @Bind("config") String config);

    @SqlQuery("select * from warehouses where team_id = :teamId and is_deleted = 0 order by id desc")
    List<Warehouse> listWarehouses(@Bind("teamId") Long teamId);


    @Slf4j
    class WarehouseRowMapper implements RowMapper<Warehouse> {

        @Override
        public Warehouse map(ResultSet rs, StatementContext ctx) throws SQLException {

            try {
                WarehouseType warehouseType = WarehouseType.valueOf(rs.getString(TableFields.TYPE));
                EncryptionManager encryptionManager = ObjectRegistry.getInstance(EncryptionManager.class);
                Long teamId = rs.getLong(TableFields.TEAM_ID);
                String encryptedConfig = rs.getString(TableFields.CONFIG);
                String configString = encryptionManager.decryptText(encryptedConfig, teamId);
                return Warehouse.builder().id(rs.getLong(TableFields.ID)).name(rs.getString(TableFields.NAME))
                        .config(JsonUtils.jsonStringToObject(configString, WarehouseConfig.class))
                        .status(WarehouseStatus.valueOf(rs.getString(TableFields.STATUS)))
                        .type(warehouseType).teamId(teamId)
                        .build();
            } catch (EncryptionException e) {
                log.error("Warehouse row mapper failed", e);
                throw new CastledRuntimeException(e);
            }
        }
    }

}
