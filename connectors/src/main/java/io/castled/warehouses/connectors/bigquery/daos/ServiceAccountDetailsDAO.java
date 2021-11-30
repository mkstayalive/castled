package io.castled.warehouses.connectors.bigquery.daos;

import io.castled.utils.JsonUtils;
import io.castled.warehouses.connectors.bigquery.GcpServiceAccount;
import io.castled.commons.models.ServiceAccountDetails;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;


@RegisterRowMapper(ServiceAccountDetailsDAO.ServiceAccountRowMapper.class)
public interface ServiceAccountDetailsDAO {

    @GetGeneratedKeys
    @SqlUpdate("insert into service_account_details(user_id, account_email, account_key) " +
            "values(:userId, :accountEmail, :accountKey)")
    long createServiceAccountDetails(@Bind("userId") long userId,
                                     @Bind("accountEmail") String accountEmail, @Bind("accountKey") String accountKey);

    @SqlQuery("select * from service_account_details where user_id = :userId")
    GcpServiceAccount getUserServiceAccount(@Bind("userId") Long userId);

    @SqlQuery("select * from service_account_details where account_email = :accountEmail")
    GcpServiceAccount getServiceAccount(@Bind("accountEmail") String accountEmail);


    @Slf4j
    class ServiceAccountRowMapper implements RowMapper<GcpServiceAccount> {
        @Override
        public GcpServiceAccount map(ResultSet rs, StatementContext ctx) throws SQLException {

            String accountKey = rs.getString("account_key");
            ServiceAccountDetails serviceAccountDetails = JsonUtils.jsonStringToObject(new String(Base64.getDecoder().decode(accountKey.getBytes(StandardCharsets.UTF_8))),
                    ServiceAccountDetails.class);
            return GcpServiceAccount.builder().id(rs.getLong("id"))
                    .userId(rs.getLong("user_id")).serviceAccountDetails(serviceAccountDetails).build();

        }
    }
}

