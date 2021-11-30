package io.castled.warehouses.connectors.bigquery.daos;

import io.castled.warehouses.connectors.bigquery.BQSnapshotTracker;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(BQSnapshotTrackerDAO.BQSnapshotTrackerRowMapper.class)
public interface BQSnapshotTrackerDAO {

    @GetGeneratedKeys
    @SqlUpdate("insert into bq_snapshot_tracker(pipeline_id, committed, uncommitted) values(:pipelineUUID, :committed, :uncommitted)")
    long createPipelineSnapshot(@Bind("pipelineUUID") String pipelineUUID, @Bind("committed") String committedSnapshot,
                                @Bind("uncommitted") String uncommittedSnapshot);

    @SqlQuery("select * from bq_snapshot_tracker where pipeline_id = :pipelineUUID")
    BQSnapshotTracker getSnapshotTracker(@Bind("pipelineUUID") String pipelineUUID);

    @SqlUpdate("update bq_snapshot_tracker set uncommitted = :uncommitted where pipeline_id = :pipelineUUID")
    int updateUncommittedSnapshot(@Bind("pipelineUUID") String pipelineUUID, @Bind("uncommitted") String uncommittedSnapshot);

    @SqlUpdate("update bq_snapshot_tracker set committed = uncommitted, uncommitted = NULL where pipeline_id = :pipelineUUID")
    int commitSnapshot(@Bind("pipelineUUID") String pipelineUUID);


    class BQSnapshotTrackerRowMapper implements RowMapper<BQSnapshotTracker> {

        @Override
        public BQSnapshotTracker map(ResultSet rs, StatementContext ctx) throws SQLException {
            return BQSnapshotTracker.builder().id(rs.getLong("id"))
                    .pipelineUUID(rs.getString("pipeline_id"))
                    .committedSnapshot(rs.getString("committed"))
                    .uncommittedSnapshot(rs.getString("uncommitted"))
                    .build();
        }
    }
}
