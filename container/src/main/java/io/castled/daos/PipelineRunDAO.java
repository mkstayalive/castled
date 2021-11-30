package io.castled.daos;


import io.castled.commons.models.PipelineSyncStats;
import io.castled.constants.TableFields;
import io.castled.models.PipelineRun;
import io.castled.models.PipelineRunStage;
import io.castled.models.PipelineRunStatus;
import io.castled.utils.JsonUtils;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@RegisterArgumentFactory(PipelineRunDAO.SyncStatsArgumentFactory.class)
@RegisterRowMapper(PipelineRunDAO.PipelineRunRowMapper.class)
public interface PipelineRunDAO {

    @GetGeneratedKeys
    @SqlUpdate("insert into pipeline_runs(pipeline_id, status, stage, run_stats) values(:pipelineId, 'PROCESSING','RUN_TRIGGERED',:stats)")
    long createPipelineRun(@Bind("pipelineId") Long pipelineId, @Bind("stats") PipelineSyncStats appSyncStats);

    @SqlUpdate("update pipeline_runs set stage =:stage where id = :id")
    void updatePipelineRunStage(@Bind("id") Long pipelineRunId, @Bind("stage") PipelineRunStage stage);

    @SqlUpdate("update pipeline_runs set status = 'PROCESSED', run_stats = :stats, processed_ts = now() where id = :id")
    void markProcessed(@Bind("id") Long runId, @Bind("stats") PipelineSyncStats appSyncStats);

    @SqlUpdate("update pipeline_runs set status = 'FAILED', failure_message = :failureMessage, processed_ts = now() where id = :id")
    void markFailed(@Bind("id") Long runId, @Bind("failureMessage") String failureMessage);

    @SqlQuery("select * from pipeline_runs where id = :id")
    PipelineRun getPipelineRun(@Bind("id") Long pipelineRunId);

    @SqlUpdate("update pipeline_runs set run_stats = :stats where id = :id")
    void updateSyncStatus(@Bind("id") Long runId, @Bind("stats") PipelineSyncStats pipelineSyncStats);

    @SqlQuery("select * from pipeline_runs where pipeline_id = :pipelineId order by id desc limit :limit")
    List<PipelineRun> getLastPipelineRuns(@Bind("pipelineId") Long pipelineId, @Bind("limit") int limit);

    class SyncStatsArgumentFactory extends AbstractArgumentFactory<PipelineSyncStats> {

        public SyncStatsArgumentFactory() {
            super(Types.VARCHAR);
        }

        @Override
        protected Argument build(PipelineSyncStats pipelineSyncStats, ConfigRegistry config) {
            return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(pipelineSyncStats));
        }
    }

    class PipelineRunRowMapper implements RowMapper<PipelineRun> {

        @Override
        public PipelineRun map(ResultSet rs, StatementContext ctx) throws SQLException {
            String syncStatsStr = rs.getString("run_stats");
            PipelineSyncStats pipelineSyncStats = Optional.ofNullable(syncStatsStr)
                    .map(syncStatsStrRef -> JsonUtils.jsonStringToObject(syncStatsStrRef, PipelineSyncStats.class))
                    .orElse(null);
            Timestamp processedTs = rs.getTimestamp("processed_ts");
            Timestamp createdTs = rs.getTimestamp("created_ts");
            return PipelineRun.builder().id(rs.getLong(TableFields.ID))
                    .pipelineId(rs.getLong(TableFields.PIPELINE_ID))
                    .processedTs(Optional.ofNullable(processedTs).map(Timestamp::getTime).orElse(null))
                    .createdTs(Optional.ofNullable(createdTs).map(Timestamp::getTime).orElse(null))
                    .status(PipelineRunStatus.valueOf(rs.getString(TableFields.STATUS)))
                    .stage(PipelineRunStage.valueOf(rs.getString("stage")))
                    .pipelineSyncStats(pipelineSyncStats).failureMessage(rs.getString(TableFields.FAILURE_MESSAGE))
                    .build();
        }
    }

}
