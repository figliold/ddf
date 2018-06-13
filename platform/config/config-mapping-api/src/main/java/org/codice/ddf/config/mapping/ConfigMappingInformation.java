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

import org.codice.ddf.config.mapping.ConfigMapping.Id;

/**
 * Extension to the {@link ConfigMappingProvider} interface to report constant information about the
 * provider.
 */
public interface ConfigMappingInformation extends ConfigMappingProvider {
  /**
   * Gets the configuration mapping names for which this provider can provide mapped dictionaries
   * for.
   *
   * <p><i>Note:</i> The reported set is not expected to change during the life of this provider
   * unless the provider is first unbound from the {@link ConfigMappingService} and then rebound.
   *
   * @return the names of all config mappings for which this provider can provide mapped
   *     dictionaries for
   */
  public String[] getNames();

  /**
   * Gets the optional instances for configuration mappings for which this provider can provide
   * mapped dictionaries for.
   *
   * <p><i>Note:</i> The reported set is not expected to change during the life of this provider
   * unless the provider is first unbound from the {@link ConfigMappingService} and then rebound.
   *
   * @return the instances of all config mappings for which this provider can provide mapped
   *     dictionaries for or an empty array if this provider can provide for all instances
   */
  public String[] getInstances();

  /**
   * Gets a ranking priority for this provider (see {@link ConfigMappingProvider} class description
   * for more details).
   *
   * <p><i>Note:</i> The provider's rank is not expected to change during the life of this provider
   * unless the provider is first unbound from the {@link ConfigMappingService} and then rebound.
   *
   * @return this provider's ranking priority
   */
  public int getRank();

  /**
   * Checks if this provider can provide mapped dictionaries for a given configuration mapping or
   * for all its instances if the identifier doesn't identify a specific instance.
   *
   * <p><i>Note:</i> This should be equivalent to finding {@link Id#getName()} in the array of names
   * returned by {@link #getNames()} and if {@link Id#getInstance()} is not present, an empty array
   * returned by {@link #getInstances()}. if {@link Id#getInstance()} is present, then the instance
   * should be part of the array of instances returned by {@link #getInstances()}.
   *
   * @param id the name of the config mapping to check if this provider can provide for
   * @return <code>true</code> if this provider can provide mapped dictionaries for the specified
   *     config mapping or for all its instances; <code>false</code> otherwise
   */
  public boolean canProvideFor(ConfigMapping.Id id);
}
