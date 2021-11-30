package io.castled.utils;

import com.google.common.collect.Lists;
import io.castled.models.QueryResults;
import io.castled.models.jdbc.JDBCColumn;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JDBCUtils {

    public static QueryResults getQueryResults(ResultSet resultSet) throws Exception {
        List<JDBCColumn> columnDetails = getColumns(resultSet.getMetaData());
        List<String> columnNames = columnDetails.stream().map(JDBCColumn::getName).collect(Collectors.toList());
        List<List<String>> rows = Lists.newArrayList();
        while (resultSet.next()) {
            List<String> rowValues = Lists.newArrayList();
            for (JDBCColumn jdbcColumn : columnDetails) {
                rowValues.add(Optional.ofNullable(resultSet.getObject(jdbcColumn.getName())).map(Object::toString).orElse(null));
            }
            rows.add(rowValues);
        }
        return new QueryResults(columnNames, rows);

    }

    public static List<JDBCColumn> getColumns(ResultSetMetaData resultSetMetaData) throws SQLException {
        List<JDBCColumn> JDBCColumnDetails = Lists.newArrayList();
        for (int column = 1; column <= resultSetMetaData.getColumnCount(); column++) {
            String columnLabel = resultSetMetaData.getColumnLabel(column);
            int columnType = resultSetMetaData.getColumnType(column);
            String typeName = resultSetMetaData.getColumnTypeName(column);
            int scale = resultSetMetaData.getScale(column);
            int precision = resultSetMetaData.getPrecision(column);
            JDBCColumnDetails.add(new JDBCColumn(columnLabel, columnType, typeName, scale, precision));
        }
        return JDBCColumnDetails;
    }


}
