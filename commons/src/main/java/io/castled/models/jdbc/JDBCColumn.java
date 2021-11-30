package io.castled.models.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JDBCColumn {

    private String name;
    private int type;
    private String typeName;
    private int scale;
    private int precision;
}
