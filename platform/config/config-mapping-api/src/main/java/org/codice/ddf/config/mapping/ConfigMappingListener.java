/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.config.mapping;

/**
 * Interface to be implemented and registered as a service in order to be notified of updates to
 * configuration mapping.
 */
public interface ConfigMappingListener {
  /**
   * Indicates a new configuration mapping was created.
   *
   * @param mapping the mapping that was just created
   */
  public void created(ConfigMapping mapping);

  /**
   * Indicates a configuration mapping was updated.
   *
   * @param mapping the mapping that was just updated
   */
  public void updated(ConfigMapping mapping);

  /**
   * Indicates a configuration mapping was deleted.
   *
   * @param mapping the mapping that was just deleted
   */
  public void removed(ConfigMapping mapping);
}
