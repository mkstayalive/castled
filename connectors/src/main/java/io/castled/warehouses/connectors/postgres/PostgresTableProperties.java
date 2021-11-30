package io.castled.warehouses.connectors.postgres;

import io.castled.warehouses.TableProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostgresTableProperties implements TableProperties {

    private List<String> primaryKeys;
}
