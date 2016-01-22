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
package monasca.api.domain.model.notificationmethod;

import java.util.List;

import monasca.common.model.domain.common.AbstractEntity;
import monasca.api.domain.model.common.Link;
import monasca.api.domain.model.common.Linked;

public class NotificationMethod extends AbstractEntity implements Linked {
  private List<Link> links;
  private String name;
  private NotificationMethodType type;
  private String address;

  public NotificationMethod() {}

  public NotificationMethod(String id, String name, NotificationMethodType type, String address) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.address = address;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    NotificationMethod other = (NotificationMethod) obj;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (!address.equals(other.address))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (type != other.type)
      return false;
    return true;
  }

  public String getAddress() {
    return address;
  }

  public String getId() {
    return id;
  }

  public List<Link> getLinks() {
    return links;
  }

  public String getName() {
    return name;
  }

  public NotificationMethodType getType() {
    return type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(NotificationMethodType type) {
    this.type = type;
  }
}
