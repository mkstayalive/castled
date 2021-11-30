package io.castled.oauth;

import com.google.inject.Singleton;
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

@Singleton
@RegisterRowMapper(OAuthDAO.OauthRowMapper.class)
@RegisterArgumentFactory(OAuthDAO.OAuthConfigArgumentFactory.class)
public interface OAuthDAO {

    @SqlUpdate("insert into oauth_details(provider, config) values(:oauth.accessConfig.provider, :oauth.accessConfig)")
    @GetGeneratedKeys
    long createOAuthDetails(@BindBean("oauth") OAuthDetails oauthDetails);

    @SqlQuery("select * from oauth_details where id = :id")
    OAuthDetails getOAuthDetails(@Bind("id") Long id);

    @SqlUpdate("update oauth_details set config = :config where id = :id")
    void updateAccessConfig(@Bind("id") Long id, @Bind("config") OAuthAccessConfig authAccessConfig);

    class OAuthConfigArgumentFactory extends AbstractArgumentFactory<OAuthAccessConfig> {

        public OAuthConfigArgumentFactory() {
            super(Types.VARCHAR);
        }

        @Override
        public Argument build(OAuthAccessConfig oauthAccessConfig, ConfigRegistry config) {
            return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(oauthAccessConfig));
        }
    }

    class OauthRowMapper implements RowMapper<OAuthDetails> {

        @Override
        public OAuthDetails map(ResultSet rs, StatementContext ctx) throws SQLException {
            OAuthAccessConfig providerConfig = JsonUtils.jsonStringToObject(rs.getString("config"), OAuthAccessConfig.class);
            return OAuthDetails.builder().
                    id(rs.getLong("id"))
                    .accessConfig(providerConfig).build();
        }
    }
}
