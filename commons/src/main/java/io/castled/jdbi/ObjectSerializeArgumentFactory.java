package io.castled.jdbi;

import io.castled.utils.JsonUtils;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class ObjectSerializeArgumentFactory extends AbstractArgumentFactory<Object> {

    public ObjectSerializeArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(Object object, ConfigRegistry registry) {
        return (position, statement, ctx) -> statement.setString(position, JsonUtils.objectToString(object));
    }
}
