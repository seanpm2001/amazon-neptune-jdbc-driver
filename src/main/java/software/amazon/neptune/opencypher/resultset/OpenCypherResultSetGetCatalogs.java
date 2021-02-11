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

import com.google.common.collect.ImmutableList;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenCypherResultSetGetCatalogs extends OpenCypherResultSetGetString {
    /**
     * TABLE_CAT String => catalog name
     */
    private static final List<String> COLUMNS = ImmutableList.of("TABLE_CAT");
    private static final Map<String, String> CONVERSION_MAP = new HashMap<>();

    static {
        CONVERSION_MAP.put("TABLE_CAT", null);
    }

    /**
     * OpenCypherResultSetGetCatalogs constructor, initializes super class.
     *
     * @param statement Statement Object.
     */
    public OpenCypherResultSetGetCatalogs(final Statement statement) {
        super(statement, 0, COLUMNS, ImmutableList.of(CONVERSION_MAP));
    }
}