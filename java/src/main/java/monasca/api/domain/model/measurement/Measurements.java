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
package monasca.api.domain.model.measurement;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import monasca.common.model.domain.common.AbstractEntity;

/**
 * Encapsulates a metric measurements.
 */
public class Measurements extends AbstractEntity {
  private static final String[] COLUMNS = new String[] {"timestamp", "value", "value_meta"};

  private String name;
  private Map<String, String> dimensions;
  private final String[] columns = COLUMNS;
  private List<Object[]> measurements;

  public Measurements() {}

  public Measurements(String name, Map<String, String> dimensions, List<Object[]> measurements) {
    this.name = name;
    this.dimensions = dimensions;
    this.measurements = measurements;
  }

  public Measurements(String name, Map<String, String> dimensions) {
    this.name = name;
    this.dimensions = dimensions;
    this.measurements = new LinkedList<>();
  }

  public void addMeasurement(Object[] measurement) {
    measurements.add(measurement);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Measurements other = (Measurements) obj;
    if (dimensions == null) {
      if (other.dimensions != null)
        return false;
    } else if (!dimensions.equals(other.dimensions))
      return false;
    if (measurements == null) {
      if (other.measurements != null)
        return false;
    } else if (!measurements.equals(other.measurements))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  public String[] getColumns() {
    return columns;
  }

  public Map<String, String> getDimensions() {
    return dimensions;
  }

  public List<Object[]> getMeasurements() {
    return measurements;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dimensions == null) ? 0 : dimensions.hashCode());
    result = prime * result + ((measurements == null) ? 0 : measurements.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  public void setDimensions(Map<String, String> dimensions) {
    this.dimensions = dimensions;
  }

  public void setMeasurements(List<Object[]> measurements) {
    this.measurements = measurements;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return String.format("Measurement [name=%s, dimensions=%s, measurements=%s]", name, dimensions,
        measurements);
  }
}
