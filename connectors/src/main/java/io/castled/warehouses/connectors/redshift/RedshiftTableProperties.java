package io.castled.warehouses.connectors.redshift;

import io.castled.warehouses.TableProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedshiftTableProperties implements TableProperties {

    private List<String> sortKeys;
    private String distributionKey;
}
