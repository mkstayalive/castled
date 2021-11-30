package io.castled.kafka;

import io.castled.utils.JsonUtils;
import org.apache.kafka.common.config.AbstractConfig;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class ConfigArgumentFactory extends AbstractArgumentFactory<AbstractConfig> {

    protected ConfigArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(AbstractConfig config, ConfigRegistry registry) {
        return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(config.originals()));
    }
}
