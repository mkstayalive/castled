package io.castled.daos;

import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.constants.TableFields;
import io.castled.dtos.PipelineConfigDTO;
import io.castled.models.*;
import io.castled.models.jobschedule.JobSchedule;
import io.castled.models.users.User;
import io.castled.utils.JsonUtils;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@RegisterArgumentFactory(PipelineDAO.JobScheduleArgumentFactory.class)
@RegisterArgumentFactory(PipelineDAO.AppSyncArgumentFactory.class)
@RegisterArgumentFactory(PipelineDAO.DataMappingArgumentFactory.class)
@RegisterRowMapper(PipelineDAO.PipelineRowMapper.class)
@RegisterRowMapper(PipelineDAO.WarehouseAggregateRowMapper.class)
@RegisterRowMapper(PipelineDAO.AppAggregateRowMapper.class)
public interface PipelineDAO {

    @GetGeneratedKeys
    @SqlUpdate("insert into pipelines(name, user_id, team_id, schedule, source_query, mapping, app_sync_config, app_id, warehouse_id, uuid, status, sync_status, query_mode)" +
            " values(:pipeline.name, :user.id, :user.teamId, :pipeline.schedule, :pipeline.sourceQuery," +
            " :pipeline.mapping, :pipeline.appSyncConfig, :pipeline.appId, :pipeline.warehouseId, :uuid, 'OK', 'ACTIVE', :pipeline.queryMode)")
    long createPipeline(@BindBean("pipeline") PipelineConfigDTO pipelineConfigDTO, @BindBean("user") User user, @Bind("uuid") String uuid);

    @SqlUpdate("update pipelines set name =:name, schedule =:schedule where id = :id")
    void updatePipeline(@Bind("id") Long id, @Bind("name") String name, @Bind("schedule") JobSchedule jobSchedule);

    @SqlQuery("select * from pipelines where id = :id and is_deleted = 0")
    Pipeline getActivePipeline(@Bind("id") Long id);

    @SqlQuery("select * from pipelines where app_id =:appId and is_deleted = 0")
    List<Pipeline> getPipelinesByAppId(@Bind("appId") Long appId);

    @SqlQuery("select * from pipelines where warehouse_id =:whId and is_deleted = 0")
    List<Pipeline> getPipelinesByWhId(@Bind("whId") Long whId);

    @SqlQuery("select * from pipelines where id = :id")
    Pipeline getPipeline(@Bind("id") Long id);

    @SqlQuery("select * from pipelines where team_id = :teamId and is_deleted = 0 order by id desc")
    List<Pipeline> listPipelines(@Bind("teamId") Long teamId);

    @SqlQuery("select * from pipelines where team_id = :teamId and app_id = :appId and is_deleted = 0 ")
    List<Pipeline> listPipelines(@Bind("teamId") Long teamId, @Bind("appId") Long appId);

    @SqlUpdate("update pipelines set sync_status = :syncStatus where id = :id")
    void updateSyncStatus(@Bind("id") Long id, @Bind("syncStatus") PipelineSyncStatus syncStatus);

    @SqlUpdate("update pipelines set is_deleted = 1 where id = :id")
    void markPipelineDeleted(@Bind("id") Long id);

    @SqlQuery("select warehouse_id, count(*) as pipelines from pipelines where is_deleted = 0 and team_id = :teamId group by warehouse_id")
    List<WarehouseAggregate> aggregateByWarehouse(@Bind("teamId") Long teamId);

    @SqlQuery("select count(id) from pipelines where is_deleted =0 and warehouse_id = :warehouseId")
    int getWarehousePipelines(@Bind("warehouseId") Long warehouseId);

    @SqlQuery("select count(id) from pipelines where is_deleted =0 and app_id = :appId")
    int getAppPipelines(@Bind("appId") Long appId);

    @SqlQuery("select app_id, count(*) as pipelines from pipelines where is_deleted = 0 and team_id = :teamId group by app_id")
    List<AppAggregate> aggregateByApp(@Bind("teamId") Long teamId);

    class JobScheduleArgumentFactory extends AbstractArgumentFactory<JobSchedule> {

        public JobScheduleArgumentFactory() {
            super(Types.VARCHAR);
        }

        @Override
        protected Argument build(JobSchedule jobSchedule, ConfigRegistry config) {
            return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(jobSchedule));
        }
    }


    class AppSyncArgumentFactory extends AbstractArgumentFactory<AppSyncConfig> {

        public AppSyncArgumentFactory() {
            super(Types.VARCHAR);
        }

        @Override
        protected Argument build(AppSyncConfig appSyncConfig, ConfigRegistry config) {
            return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(appSyncConfig));
        }
    }


    class DataMappingArgumentFactory extends AbstractArgumentFactory<CastledDataMapping> {

        public DataMappingArgumentFactory() {
            super(Types.VARCHAR);
        }

        @Override
        protected Argument build(CastledDataMapping dataMapping, ConfigRegistry config) {
            return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(dataMapping));
        }
    }

    class WarehouseAggregateRowMapper implements RowMapper<WarehouseAggregate> {

        @Override
        public WarehouseAggregate map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new WarehouseAggregate(rs.getLong("warehouse_id"),
                    rs.getInt("pipelines"));
        }
    }

    class AppAggregateRowMapper implements RowMapper<AppAggregate> {

        @Override
        public AppAggregate map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new AppAggregate(rs.getLong("app_id"),
                    rs.getInt("pipelines"));
        }
    }

    class PipelineRowMapper implements RowMapper<Pipeline> {

        @Override
        public Pipeline map(ResultSet rs, StatementContext ctx) throws SQLException {
            AppSyncConfig appSyncConfig = JsonUtils.jsonStringToObject(rs.getString("app_sync_config"), AppSyncConfig.class);
            CastledDataMapping mapping = JsonUtils.jsonStringToObject(rs.getString("mapping"), CastledDataMapping.class);
            JobSchedule jobSchedule = JsonUtils.jsonStringToObject(rs.getString(TableFields.SCHEDULE), JobSchedule.class);

            return Pipeline.builder().id(rs.getLong(TableFields.ID)).name(rs.getString(TableFields.NAME))
                    .status(PipelineStatus.valueOf(rs.getString(TableFields.STATUS)))
                    .seqId(rs.getLong(TableFields.SEQ_ID)).appSyncConfig(appSyncConfig)
                    .dataMapping(mapping).uuid(rs.getString(TableFields.UUID)).isDeleted(rs.getBoolean(TableFields.ID))
                    .jobSchedule(jobSchedule).sourceQuery(rs.getString("source_query"))
                    .teamId(rs.getLong(TableFields.TEAM_ID)).queryMode(QueryMode.valueOf(rs.getString("query_mode")))
                    .appId(rs.getLong(TableFields.APP_ID)).warehouseId(rs.getLong(TableFields.WAREHOUSE_ID))
                    .syncStatus(PipelineSyncStatus.valueOf(rs.getString("sync_status"))).build();
        }
    }
}
