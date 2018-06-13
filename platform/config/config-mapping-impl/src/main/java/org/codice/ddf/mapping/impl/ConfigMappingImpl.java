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
package org.codice.ddf.mapping.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codice.ddf.config.ConfigService;
import org.codice.ddf.config.mapping.ConfigMapping;
import org.codice.ddf.config.mapping.ConfigMappingException;
import org.codice.ddf.config.mapping.ConfigMappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigMappingImpl implements ConfigMapping {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMappingImpl.class);

  private final ConfigService config;

  private final ConfigMapping.Id id;

  private final SortedSet<ConfigMappingProvider> providers;

  public ConfigMappingImpl(
      ConfigService config, ConfigMapping.Id id, Stream<ConfigMappingProvider> providers) {
    this.config = config;
    this.id = id;
    this.providers =
        Collections.synchronizedSortedSet(providers.collect(Collectors.toCollection(TreeSet::new)));
  }

  @Override
  public Id getId() {
    return id;
  }

  /**
   * Checks if this config mapping currently has any providers for it.
   *
   * @return <code>true</code> if at least one provider is capable of providing mapped properties
   *     for this config mapping; <code>false</code> otherwise
   */
  public boolean isAvailable() {
    return !providers.isEmpty();
  }

  public boolean bind(ConfigMappingProvider provider) {
    final boolean bound = providers.add(provider);

    LOGGER.debug("ConfigMappingImpl[{}].bind({}) = {}", id, provider, bound);
    return bound;
  }

  public boolean unbind(ConfigMappingProvider provider) {
    final boolean unbound = providers.remove(provider);

    LOGGER.debug("ConfigMappingImpl[{}].unbind({}) = {}", id, provider, unbound);
    return unbound;
  }

  @Override
  public Map<String, Object> resolve() throws ConfigMappingException {
    final Map<String, Object> properties = new HashMap<>();

    synchronized (providers) {
      // process them from lowes priority to highest such that higher one can override
      providers.stream().map(p -> p.provide(id, config)).forEach(properties::putAll);
    }
    LOGGER.debug("ConfigMappingImpl[{}].resolve() = {}", id, properties);
    return properties;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof ConfigMappingImpl) {
      return id.equals(((ConfigMappingImpl) obj).id);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("ConfigMappingImpl[%s, providers=%s]", id, providers);
  }
}
