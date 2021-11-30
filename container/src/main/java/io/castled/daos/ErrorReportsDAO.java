package io.castled.daos;

import io.castled.constants.TableFields;
import io.castled.models.ErrorReport;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(ErrorReportsDAO.ErrorReportRowMapper.class)
public interface ErrorReportsDAO {

    @GetGeneratedKeys
    @SqlUpdate("insert into error_reports(pipeline_id, pipeline_run_id, report) values(:errorReport.pipelineId, " +
            ":errorReport.pipelineRunId, :errorReport.report)")
    long createErrorReport(@BindBean("errorReport") ErrorReport errorReport);

    @SqlQuery("select * from error_reports where pipeline_run_id = :pipelineRunId")
    ErrorReport getErrorReport(@Bind("pipelineRunId") Long pipelineRunId);


    class ErrorReportRowMapper implements RowMapper<ErrorReport> {
        @Override
        public ErrorReport map(ResultSet rs, StatementContext ctx) throws SQLException {
            return ErrorReport.builder()
                    .id(rs.getLong(TableFields.ID))
                    .pipelineId(rs.getLong(TableFields.PIPELINE_ID))
                    .pipelineRunId(rs.getLong(TableFields.PIPELINE_RUN_ID))
                    .report(rs.getString("report"))
                    .build();
        }
    }
}
