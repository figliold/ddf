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
package org.codice.ddf.config;

import java.util.Optional;
import java.util.stream.Stream;

/** Service interface for configuration. */
public interface ConfigService {
  /**
   * Retrieves a singleton configuration of a given type.
   *
   * @param <T> the type of singleton config to retrieve
   * @param type the type of singleton config to retrieve
   * @return the corresponding config object or empty if it doesn't exist
   */
  public <T extends ConfigSingleton> Optional<T> get(Class<T> type);

  /**
   * Retrieves a specific configuration instance of a given type of configuration group.
   *
   * @param <T> the type of group config to retrieve an instance for
   * @param type the type of group config to retrieve an instance for
   * @param id the unique instance id for the config object to retrieve
   * @return the corresponding config object instance or empty if none exist
   */
  public <T extends ConfigGroup> Optional<T> get(Class<T> type, String id);

  /**
   * Retrieves all instances of a given type of configuration group.
   *
   * @param <T> the type of group config to retrieve all instances for
   * @param type the type of group config to retrieve all instances for
   * @return a corresponding stream of all config objects
   */
  public <T extends ConfigGroup> Stream<T> configs(Class<T> type);
}
