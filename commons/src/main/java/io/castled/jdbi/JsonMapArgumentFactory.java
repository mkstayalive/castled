package io.castled.jdbi;

import io.castled.utils.JsonUtils;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;
import java.util.Map;

public class JsonMapArgumentFactory extends AbstractArgumentFactory<Map<String, Object>> {

    protected JsonMapArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(Map<String, Object> config, ConfigRegistry registry) {
        return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(config));
    }
}
