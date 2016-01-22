/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package monasca.api.infrastructure.persistence.vertica;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

@Test
public class MetricQueriesTest {

  private final static String TABLE_TO_JOIN_DIMENSIONS_ON = "defdims";

  public void metricQueriesBuildDimensionAndClauseTest1() {
    String expectedResult =
        " and defdims.dimension_set_id in (select dimension_set_id from MonMetrics.Dimensions "
        + "where name = :dname0 and value = :dvalue0 or name = :dname1 and value = :dvalue1 "
        + "group by dimension_set_id  having count(*) = 2) ";

    Map<String, String> dimsMap = new HashMap<>();
    dimsMap.put("foo", "bar");
    dimsMap.put("biz", "baz");

    String s = MetricQueries.buildDimensionAndClause(dimsMap, TABLE_TO_JOIN_DIMENSIONS_ON);
    assertEquals(expectedResult, s);
  }

  public void metricQueriesBuildDimensionAndClauseTest2() {
    String expectedResult = "";
    Map<String, String> dimsMap = new HashMap<>();
    assertEquals(expectedResult, MetricQueries.buildDimensionAndClause(dimsMap,TABLE_TO_JOIN_DIMENSIONS_ON));
  }

  public void metricQueriesBuildDimensionAndClauseForTest3() {
    String expectedResult = "";
    Map<String, String> dimsMap = null;
    assertEquals(expectedResult, MetricQueries.buildDimensionAndClause(dimsMap, TABLE_TO_JOIN_DIMENSIONS_ON));
  }
}
