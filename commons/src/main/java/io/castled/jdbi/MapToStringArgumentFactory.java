package io.castled.jdbi;

import io.castled.utils.JsonUtils;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;
import java.util.Map;

public class MapToStringArgumentFactory extends AbstractArgumentFactory<Map<String, Object>> {

    public MapToStringArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(Map<String, Object> value, ConfigRegistry config) {
        return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(config));

    }
}
