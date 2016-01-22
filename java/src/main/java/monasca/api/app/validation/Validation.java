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
package monasca.api.app.validation;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import com.fasterxml.jackson.databind.JsonMappingException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import monasca.api.resource.exception.Exceptions;

/**
 * Validation related utilities.
 */
public final class Validation {
  private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
  private static final Splitter COLON_SPLITTER = Splitter.on(':').omitEmptyStrings().trimResults().limit(2);
  private static final DateTimeFormatter ISO_8601_FORMATTER = ISODateTimeFormat
      .dateOptionalTimeParser().withZoneUTC();
  private static final List<String> VALID_STATISTICS = Arrays.asList("avg", "min", "max", "sum",
      "count");
  private static final List<String> VALID_ALARM_STATE = Arrays
      .asList("undetermined", "ok", "alarm");

  private Validation() {}

  public static final String DEFAULT_ADMIN_ROLE = "monasca-admin";

  /**
   * @throws JsonMappingException if the {@code value} is not valid for the {@code type}
   */
  public static <T extends Enum<T>> T parseAndValidate(Class<T> type, String value)
      throws JsonMappingException {
    for (T constant : type.getEnumConstants())
      if (constant.name().equalsIgnoreCase(value))
        return constant;
    List<String> acceptedValues = new ArrayList<>();
    for (T constant : type.getEnumConstants())
      acceptedValues.add(constant.name());
    throw new JsonMappingException(String.format("%s was not one of %s", value, acceptedValues));
  }

  /**
   * @throws WebApplicationException if the {@code date} invalid or is required and null.
   */
  public static DateTime parseAndValidateDate(String date, String parameterName, boolean required) {
    if (Strings.isNullOrEmpty(date)) {
      if (required)
        throw Exceptions.unprocessableEntity("%s is required", parameterName);
      else
        return null;
    }

    try {
      return ISO_8601_FORMATTER.parseDateTime(date);
    } catch (Exception e) {
      throw Exceptions.unprocessableEntity("%s (%s) must be an ISO 8601 formatted time", parameterName, date);
    }
  }

  /**
   * @throws WebApplicationException if the {@code value} is null or empty.
   */
  public static Map<String, String> parseAndValidateDimensions(String dimensionsStr) {
    Validation.validateNotNullOrEmpty(dimensionsStr, "dimensions");

    Map<String, String> dimensions = new HashMap<String, String>();
    for (String dimensionStr : COMMA_SPLITTER.split(dimensionsStr)) {
      String[] dimensionArr = Iterables.toArray(COLON_SPLITTER.split(dimensionStr), String.class);
      if (dimensionArr.length == 2)
        dimensions.put(dimensionArr[0], dimensionArr[1]);
    }

    DimensionValidation.validate(dimensions);
    return dimensions;
  }

  /**
   * @throws WebApplicationException if the {@code number} is invalid.
   */
  public static int parseAndValidateNumber(String number, String parameterName) {
    try {
      return Integer.parseInt(number);
    } catch (NumberFormatException e) {
      throw Exceptions.unprocessableEntity("%s (%s) must be valid number", parameterName, number);
    }
  }

  /**
   * @throws WebApplicationException if the {@code statistics} empty or invalid.
   */
  public static List<String> parseValidateAndNormalizeStatistics(Iterable<String> statistics) {
    List<String> validStats = new ArrayList<String>(5);
    for (String statistic : statistics) {
      String statisticLower = statistic.toLowerCase();
      if (!VALID_STATISTICS.contains(statisticLower))
        throw Exceptions.unprocessableEntity("%s is not a valid statistic", statistic);
      validStats.add(statisticLower);
    }

    if (validStats.isEmpty())
      throw Exceptions.unprocessableEntity("Statistics are required");

    return validStats;
  }

  /**
   * @throws WebApplicationException if the {@code statistics} empty or invalid.
   */
  public static void validateAlarmState(String state) {
    String stateLower = state.toLowerCase();
    if (!VALID_ALARM_STATE.contains(stateLower)) {
      throw Exceptions.unprocessableEntity("%s is not a valid state", state);
    }
  }

  /**
   * @throws WebApplicationException if the {@code value} is null or empty.
   */
  public static void validateNotNullOrEmpty(String value, String parameterName) {
    if (Strings.isNullOrEmpty(value))
      throw Exceptions.unprocessableEntity("%s is required", parameterName);
  }

  /**
   * @throws WebApplicationException if the {@code startTime} or {@code endTime} are invalid
   */
  public static void validateTimes(DateTime startTime, DateTime endTime) {
    if (endTime != null && !startTime.isBefore(endTime))
        throw Exceptions.badRequest("start_time (%s) must be before end_time (%s)", startTime, endTime);
  }

  public static Boolean validateAndParseMergeMetricsFlag(String mergeMetricsFlag) {

    if (mergeMetricsFlag == null) {

      return false;

    } else if (!"true".equalsIgnoreCase(mergeMetricsFlag)
               && !"false".equalsIgnoreCase(mergeMetricsFlag)) {

      throw Exceptions.badRequest("merge_metrics must be either 'true' or 'false'");

    } else {

      return Boolean.parseBoolean(mergeMetricsFlag);
    }
  }

  public static void validateLifecycleState(String lifecycleState) {
    if (lifecycleState != null) {
      if (lifecycleState.length() > 50) {
        throw Exceptions
            .unprocessableEntity("Lifecycle state '%s' must be 50 characters or less",
                                 lifecycleState);
      }
    }
  }

  public static void validateLink(String link) {
    if (link != null) {
      if (link.length() > 512) {
        throw Exceptions.unprocessableEntity("Link '%s' must be 512 characters or less", link);
      }
    }
  }

  /**
   * Convenience method for checking cross project access
   */
  public static String getQueryProject(String roles,
                                       String crossTenantId,
                                       String tenantId,
                                       String admin_role) throws Exception
  {
    String queryTenantId = tenantId;

    boolean isAdmin = !Strings.isNullOrEmpty(roles) &&
                      COMMA_SPLITTER.splitToList(roles).contains(admin_role);

    if (isCrossProjectRequest(crossTenantId, tenantId)) {
      if (isAdmin) {
        queryTenantId = crossTenantId;
      } else {
        throw Exceptions.forbidden("Only users with %s role can GET cross tenant metrics",
                                   admin_role);
      }
    }

    return queryTenantId;
  }

  /**
   * Convenience method for determining if request is across projects.
   */
  public static boolean isCrossProjectRequest(String crossTenantId, String tenantId) {
    return !Strings.isNullOrEmpty(crossTenantId) && !crossTenantId.equals(tenantId);
  }
}
