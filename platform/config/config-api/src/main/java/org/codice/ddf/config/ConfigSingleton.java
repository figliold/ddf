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

/**
 * Base interface for all configuration classes that are defined using a single instance. The type
 * of singleton configuration objects is defined as the first interface that extends this interface.
 */
public interface ConfigSingleton extends Config {
  /**
   * Gets the type of singleton configuration object this is.
   *
   * @return the type of config object this is
   */
  @Override
  public default Class<? extends ConfigSingleton> getType() {
    return (Class<? extends ConfigSingleton>) Config.getType(getClass());
  }
}