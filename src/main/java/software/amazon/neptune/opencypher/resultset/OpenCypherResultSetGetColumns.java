/*
 * Copyright <2020> Amazon.com, final Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, final Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, final WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, final either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package software.amazon.neptune.opencypher.resultset;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.jdbc.utilities.JavaToJdbcTypeConverter;
import software.amazon.jdbc.utilities.JdbcType;
import software.amazon.jdbc.utilities.SqlError;
import software.amazon.neptune.opencypher.OpenCypherTypeMapping;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class OpenCypherResultSetGetColumns extends OpenCypherResultSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCypherResultSetGetColumns.class);
    /**
     * TABLE_CAT String => table catalog (may be null)
     * TABLE_SCHEM String => table schema (may be null)
     * TABLE_NAME String => table name
     * COLUMN_NAME String => column name
     * DATA_TYPE int => SQL type from java.sql.Types
     * TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified
     * COLUMN_SIZE int => column size.
     * BUFFER_LENGTH is not used.
     * DECIMAL_DIGITS int => the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable.
     * NUM_PREC_RADIX int => Radix (typically either 10 or 2)
     * NULLABLE int => is NULL allowed.
     * columnNoNulls - might not allow NULL values
     * columnNullable - definitely allows NULL values
     * columnNullableUnknown - nullability unknown
     * REMARKS String => comment describing column (may be null)
     * COLUMN_DEF String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be null)
     * SQL_DATA_TYPE int => unused
     * SQL_DATETIME_SUB int => unused
     * CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column
     * ORDINAL_POSITION int => index of column in table (starting at 1)
     * IS_NULLABLE String => ISO rules are used to determine the nullability for a column.
     * YES --- if the column can include NULLs
     * NO --- if the column cannot include NULLs
     * empty string --- if the nullability for the column is unknown
     * SCOPE_CATALOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF)
     * SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF)
     * SCOPE_TABLE String => table name that this the scope of a reference attribute (null if the DATA_TYPE isn't REF)
     * SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type, SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF)
     * IS_AUTOINCREMENT String => Indicates whether this column is auto incremented
     * YES --- if the column is auto incremented
     * NO --- if the column is not auto incremented
     * empty string --- if it cannot be determined whether the column is auto incremented
     * IS_GENERATEDCOLUMN String => Indicates whether this is a generated column
     * YES --- if this a generated column
     * NO --- if this not a generated column
     * empty string --- if it cannot be determined whether this is a generated column
     */
    private static final Map<String, Object> CONVERSION_MAP = new HashMap<>();
    private static final List<String> ORDERED_COLUMNS = new ArrayList<>();
    private static final Map<String, Type> COLUMN_TYPE_MAP = new HashMap<>();

    static {
        CONVERSION_MAP.put("TABLE_CAT", null); // null
        CONVERSION_MAP.put("TABLE_SCHEM", null); // null
        CONVERSION_MAP.put("BUFFER_LENGTH", null); // null
        CONVERSION_MAP.put("NULLABLE", DatabaseMetaData.columnNullable);
        CONVERSION_MAP.put("REMARKS", null); // null
        CONVERSION_MAP.put("SQL_DATA_TYPE", null); // null
        CONVERSION_MAP.put("SQL_DATETIME_SUB", null); // null
        CONVERSION_MAP.put("IS_NULLABLE", "YES");
        CONVERSION_MAP.put("SCOPE_CATALOG", null); // null
        CONVERSION_MAP.put("SCOPE_SCHEMA", null); // null
        CONVERSION_MAP.put("SCOPE_TABLE", null); // null
        CONVERSION_MAP.put("SOURCE_DATA_TYPE", null); // null
        CONVERSION_MAP.put("IS_AUTOINCREMENT", "NO");
        CONVERSION_MAP.put("IS_GENERATEDCOLUMN", "NO");
        CONVERSION_MAP.put("COLUMN_DEF", null);

        ORDERED_COLUMNS.add("TABLE_CAT");
        COLUMN_TYPE_MAP.put("TABLE_CAT", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("TABLE_SCHEM");
        COLUMN_TYPE_MAP.put("TABLE_SCHEM", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("TABLE_NAME");
        COLUMN_TYPE_MAP.put("TABLE_NAME", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("COLUMN_NAME");
        COLUMN_TYPE_MAP.put("COLUMN_NAME", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("DATA_TYPE");
        COLUMN_TYPE_MAP.put("DATA_TYPE", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("TYPE_NAME");
        COLUMN_TYPE_MAP.put("TYPE_NAME", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("COLUMN_SIZE");
        COLUMN_TYPE_MAP.put("COLUMN_SIZE", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("BUFFER_LENGTH");
        COLUMN_TYPE_MAP.put("BUFFER_LENGTH", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("DECIMAL_DIGITS");
        COLUMN_TYPE_MAP.put("DECIMAL_DIGITS", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("NUM_PREC_RADIX");
        COLUMN_TYPE_MAP.put("NUM_PREC_RADIX", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("NULLABLE");
        COLUMN_TYPE_MAP.put("NULLABLE", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("REMARKS");
        COLUMN_TYPE_MAP.put("REMARKS", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("COLUMN_DEF");
        COLUMN_TYPE_MAP.put("COLUMN_DEF", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("SQL_DATA_TYPE");
        COLUMN_TYPE_MAP.put("SQL_DATA_TYPE", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("SQL_DATETIME_SUB");
        COLUMN_TYPE_MAP.put("SQL_DATETIME_SUB", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("CHAR_OCTET_LENGTH");
        COLUMN_TYPE_MAP.put("CHAR_OCTET_LENGTH", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("ORDINAL_POSITION");
        COLUMN_TYPE_MAP.put("ORDINAL_POSITION", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("IS_NULLABLE");
        COLUMN_TYPE_MAP.put("IS_NULLABLE", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("SCOPE_CATALOG");
        COLUMN_TYPE_MAP.put("SCOPE_CATALOG", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("SCOPE_SCHEMA");
        COLUMN_TYPE_MAP.put("SCOPE_SCHEMA", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("SCOPE_TABLE");
        COLUMN_TYPE_MAP.put("SCOPE_TABLE", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("SOURCE_DATA_TYPE");
        COLUMN_TYPE_MAP.put("SOURCE_DATA_TYPE", InternalTypeSystem.TYPE_SYSTEM.INTEGER());
        ORDERED_COLUMNS.add("IS_AUTOINCREMENT");
        COLUMN_TYPE_MAP.put("IS_AUTOINCREMENT", InternalTypeSystem.TYPE_SYSTEM.STRING());
        ORDERED_COLUMNS.add("IS_GENERATEDCOLUMN");
        COLUMN_TYPE_MAP.put("IS_GENERATEDCOLUMN", InternalTypeSystem.TYPE_SYSTEM.STRING());
    }

    private final List<Map<String, Object>> rows = new ArrayList<>();

    /**
     * OpenCypherResultSetGetCatalogs constructor, initializes super class.
     *
     * @param statement       Statement Object.
     * @param nodeColumnInfos List of NodeColumnInfo Objects.
     */
    public OpenCypherResultSetGetColumns(final Statement statement, final List<NodeColumnInfo> nodeColumnInfos)
            throws Exception {
        super(statement, null, null, nodeColumnInfos.size(), ORDERED_COLUMNS);
        for (final NodeColumnInfo nodeColumnInfo : nodeColumnInfos) {
            // Add defaults.
            final Map<String, Object> map = new HashMap<>(CONVERSION_MAP);

            // Set table name.
            map.put("TABLE_NAME", OpenCypherResultSetGetTables.nodeListToString(nodeColumnInfo.labels));
            int i = 1;
            for (final Map<String, Object> property : nodeColumnInfo.properties) {
                // Get column type.
                final String dataType = property.get("dataType").toString();
                map.put("TYPE_NAME", dataType);
                final Class<?> javaClass = OpenCypherTypeMapping.GREMLIN_STRING_TYPE_TO_JAVA_TYPE_CONVERTER_MAP
                        .getOrDefault(dataType, String.class);
                map.put("CHAR_OCTET_LENGTH", (javaClass == String.class) ? Integer.MAX_VALUE : null);
                final int jdbcType = JavaToJdbcTypeConverter.CLASS_TO_JDBC_ORDINAL
                        .getOrDefault(javaClass, JdbcType.VARCHAR.getJdbcCode());
                map.put("DATA_TYPE", jdbcType);

                map.put("COLUMN_NAME", property.getOrDefault("property", "unknown"));
                final Object nullable = property.getOrDefault("isNullable", null);
                if (!(nullable instanceof Boolean)) {
                    map.put("NULLABLE", DatabaseMetaData.columnNullableUnknown);
                    map.put("IS_NULLABLE", "");
                } else {
                    map.put("NULLABLE",
                            (Boolean) nullable ? DatabaseMetaData.columnNullable : DatabaseMetaData.columnNoNulls);
                    map.put("IS_NULLABLE", (Boolean) nullable ? "YES" : "NO");
                }

                // TODO: These need to be verified for Tableau.
                map.put("DECIMAL_DIGITS", null);
                map.put("NUM_PREC_RADIX", 10);
                map.put("ORDINAL_POSITION", i++);
                map.put("COLUMN_SIZE", null);
            }
            if (!map.keySet().equals(new HashSet<>(ORDERED_COLUMNS))) {
                throw new SQLException(
                        "Encountered error while parsing column metadata. Map key set does not match expected key set.");
            }
            rows.add(map);
        }
    }

    @Override
    protected ResultSetMetaData getOpenCypherMetadata() {
        final List<Type> rowTypes = new ArrayList<>();
        for (final String column : ORDERED_COLUMNS) {
            rowTypes.add(COLUMN_TYPE_MAP.get(column));
        }
        return new OpenCypherResultSetMetadata(ORDERED_COLUMNS, rowTypes);
    }

    @Override
    protected Object getConvertedValue(final int columnIndex) throws SQLException {
        verifyOpen();
        final int index = getRowIndex();
        if ((index >= rows.size()) || (index < 0)) {
            throw SqlError
                    .createSQLFeatureNotSupportedException(LOGGER, SqlError.INVALID_INDEX, index + 1,
                            rows.size());
        } else if ((columnIndex > ORDERED_COLUMNS.size()) || (columnIndex <= 0)) {
            throw SqlError
                    .createSQLFeatureNotSupportedException(LOGGER, SqlError.INVALID_COLUMN_INDEX, columnIndex,
                            ORDERED_COLUMNS.size());
        }
        final String key = ORDERED_COLUMNS.get(columnIndex - 1);
        if (rows.get(index).containsKey(key)) {
            return rows.get(index).get(key);
        } else {
            throw SqlError
                    .createSQLFeatureNotSupportedException(LOGGER, SqlError.INVALID_COLUMN_LABEL, key);
        }
    }

    @AllArgsConstructor
    public static class NodeColumnInfo {
        @Getter
        private final List<String> labels;
        @Getter
        private final List<Map<String, Object>> properties;

        @Override
        public boolean equals(final Object nodeColumnInfo) {
            if (!(nodeColumnInfo instanceof NodeColumnInfo)) {
                return false;
            }
            final NodeColumnInfo nodeInfo = (NodeColumnInfo) (nodeColumnInfo);
            return nodeInfo.labels.equals(this.labels) && nodeInfo.properties.equals(this.properties);
        }
    }
}