package io.castled.jarvis.taskmanager.daos;

import io.castled.constants.TableFields;
import io.castled.jarvis.taskmanager.JarvisConstants;
import io.castled.jarvis.taskmanager.models.RetryCriteria;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.jarvis.taskmanager.models.TaskPriority;
import io.castled.jarvis.taskmanager.models.TaskStatus;
import io.castled.jarvis.taskmanager.models.requests.TaskCreateRequest;
import io.castled.jdbi.MapToStringArgumentFactory;
import io.castled.jdbi.ObjectSerializeArgumentFactory;
import io.castled.utils.JsonUtils;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ObjectArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RegisterRowMapper(JarvisTasksDAO.JarvisTaskMapper.class)
@RegisterArgumentFactory(JarvisTasksDAO.RetryCriteriaArgumentFactory.class)
public interface JarvisTasksDAO {

    @SqlQuery("select * from jarvis_tasks where id = :id")
    Task getTask(@Bind("id") Long taskId);

    @SqlUpdate("insert into jarvis_tasks(priority, status, unique_id, type, task_group, expiry, retry_criteria, params, search_id) " +
            "values(:tcr.priority, 'QUEUED', :tcr.uniqueId, :tcr.type, :tcr.group, :tcr.expiry, :tcr.retryCriteria, :tcr.params, :tcr.searchId)")
    @GetGeneratedKeys
    long createTask(@BindBean("tcr") TaskCreateRequest taskCreateRequest);

    @SqlBatch("update jarvis_tasks set status = :status, refresh_ts = now() where id = :id")
    void updateTaskStatus(@Bind("id") List<Long> taskIds, @Bind("status") TaskStatus status);

    @SqlBatch("update jarvis_tasks set priority = :priority, refresh_ts = now() where id = :id")
    void updateTaskPriority(@Bind("id") Collection<Long> taskIds, @Bind("priority") Collection<TaskPriority> priorities);

    @SqlUpdate("update jarvis_tasks set status = :status, failure_message =:failureMessage, attempts = :attempts, refresh_ts = now() where id = :id")
    void markFailed(@Bind("id") Long taskId, @Bind("status") TaskStatus taskStatus,
                    @Bind("failureMessage") String failureMessage, @Bind("attempts") int attempts);

    @SqlUpdate("update jarvis_tasks set status = 'DEFERRED', deferred_till = :deferredTill, refresh_ts = now() where id = :id")
    void markDeferred(@Bind("id") Long taskId, @Bind("deferredTill") Timestamp deferredTill);

    @SqlUpdate("update jarvis_tasks set status = 'PROCESSED', result =:result where id = :id")
    void markTaskProcessed(@Bind("id") Long taskId, @Bind("result") String result);

    @SqlQuery("select * from jarvis_tasks where status = :status order by id limit :limit")
    List<Task> getTasksInStatus(@Bind("status") TaskStatus taskStatus, @Bind("limit") int limit);

    @SqlQuery("select count(*) from jarvis_tasks where type = :type and unique_id = :uniqueId and status in (<status>)")
    int getTasksCount(@Bind("type") String type, @Bind("uniqueId") String uniqueId, @BindList("status") List<TaskStatus> taskStatuses);

    @SqlQuery("select * from jarvis_tasks where status = 'DEFERRED' and deferred_till < now() order by id limit :limit")
    List<Task> getRetriableDeferredTasks(@Bind("limit") int limit);

    @SqlQuery("select * from jarvis_tasks where status in (<status>) and refresh_ts < now() - INTERVAL :refreshOffset MINUTE and id > :id order by id limit :limit")
    List<Task> getUnrefreshedTasks(@Bind("refreshOffset") long refreshOffsetMins, @Bind("id") long id,
                                   @BindList("status") List<TaskStatus> taskStatuses, @Bind("limit") int limit);


    @SqlQuery("select * from jarvis_tasks where search_id = :searchId and type = :type order by id")
    List<Task> getTasksBySearchId(@Bind("searchId") String searchId, @Bind("type") String taskType);


    class RetryCriteriaArgumentFactory extends AbstractArgumentFactory<RetryCriteria> {

        public RetryCriteriaArgumentFactory() {
            super(Types.VARCHAR);
        }

        @Override
        public Argument build(RetryCriteria retryCriteria, ConfigRegistry config) {
            return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(retryCriteria));
        }
    }

    class JarvisTaskMapper implements RowMapper<Task> {

        @Override
        public Task map(ResultSet rs, StatementContext ctx) throws SQLException {

            RetryCriteria retryCriteria = JsonUtils.jsonStringToObject(rs.getString("retry_criteria"), RetryCriteria.class);
            String paramsString = rs.getString(TableFields.PARAMS);
            Map<String, Object> params = Optional.ofNullable(paramsString).map(JsonUtils::jsonStringToMap).orElse(null);
            return Task.builder()
                    .id(rs.getLong(JarvisConstants.ID_FIELD)).group(rs.getString("task_group"))
                    .status(Optional.ofNullable(rs.getString("status")).map(TaskStatus::valueOf).orElse(null))
                    .priority(TaskPriority.valueOf(rs.getString("priority")))
                    .type(rs.getString("type")).uniqueId(rs.getString("unique_id"))
                    .expiry(rs.getLong("expiry")).retryCriteria(retryCriteria)
                    .attempts(rs.getInt("attempts")).params(params)
                    .searchId(rs.getString("search_id")).result(rs.getString(TableFields.RESULT))
                    .failureMessage(rs.getString(TableFields.FAILURE_MESSAGE))
                    .build();
        }
    }

}
