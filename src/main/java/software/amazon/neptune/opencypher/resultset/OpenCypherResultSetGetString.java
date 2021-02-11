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

import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.jdbc.utilities.SqlError;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenCypherResultSetGetString extends OpenCypherResultSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCypherResultSetGetString.class);
    /**
     * TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     */
    private final int rowCount;
    private final List<String> columns;
    private final List<Map<String, String>> constantReturns;

    /**
     * OpenCypherResultSetGetString constructor, initializes super class.
     *
     * @param statement       Statement Object.
     * @param rowCount        Number of rows.
     * @param columns         List of ordered columns.
     * @param constantReturns Map of return values for given keys.
     */
    public OpenCypherResultSetGetString(final Statement statement, final int rowCount, final List<String> columns,
                                        final List<Map<String, String>> constantReturns) {
        super(statement, null, null, rowCount, columns);
        this.rowCount = rowCount;
        this.columns = columns;
        this.constantReturns = constantReturns;
    }

    @Override
    protected ResultSetMetaData getOpenCypherMetadata() throws SQLException {
        final List<Type> rowTypes = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            rowTypes.add(InternalTypeSystem.TYPE_SYSTEM.STRING());
        }
        return new OpenCypherResultSetMetadata(columns, rowTypes);
    }

    @Override
    protected Object getConvertedValue(final int columnIndex) throws SQLException {
        verifyOpen();
        final int index = getRowIndex();
        if ((index >= constantReturns.size()) || (index < 0)) {
            throw SqlError
                    .createSQLFeatureNotSupportedException(LOGGER, SqlError.INVALID_INDEX, index + 1,
                            constantReturns.size());
        } else if ((columnIndex > columns.size()) || (columnIndex <= 0)) {
            throw SqlError
                    .createSQLFeatureNotSupportedException(LOGGER, SqlError.INVALID_COLUMN_INDEX, columnIndex,
                            columns.size());
        }
        final String key = columns.get(columnIndex - 1);
        if (constantReturns.get(index).containsKey(key)) {
            return constantReturns.get(index).get(key);
        } else {
            throw SqlError
                    .createSQLFeatureNotSupportedException(LOGGER, SqlError.INVALID_COLUMN_LABEL, key);
        }
    }
}