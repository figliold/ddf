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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.codice.ddf.config.Config;
import org.codice.ddf.config.mapping.ConfigMapping;
import org.codice.ddf.config.mapping.ConfigMappingInformation;
import org.codice.ddf.config.mapping.ConfigMappingListener;
import org.codice.ddf.config.mapping.ConfigMappingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigMappingServiceImpl implements ConfigMappingService {
  // , ConfigAbstractionListener
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMappingServiceImpl.class);

  private final Config config;

  private final SortedSet<ConfigMappingInformation> providers =
      Collections.synchronizedSortedSet(new TreeSet<>());

  private final List<ConfigMappingListener> listeners;

  private final Map<ConfigMapping.Id, ConfigMappingImpl> mappings = new ConcurrentHashMap<>();

  public ConfigMappingServiceImpl(Config config, List<ConfigMappingListener> listeners) {
    this.config = config;
    this.listeners = listeners;
  }

  @Override
  public boolean bind(ConfigMappingInformation provider) {
    if (providers.add(provider)) {
      LOGGER.debug("bound provider: {}", provider);
      // find config mapping that needs to be updated
      mappings
          .values()
          .stream()
          .filter(m -> provider.canProvideFor(m.getId()))
          .forEach(m -> bind(provider, m));
      return true;
    }
    return false;
  }

  @Override
  public boolean unbind(ConfigMappingInformation provider) {
    if (providers.remove(provider)) {
      LOGGER.debug("unbound provider: {}", provider);
      // find config mapping that needs to be updated
      mappings.forEach((id, m) -> unbind(provider, m));
      return true;
    }
    return false;
  }

  @Override
  public Optional<ConfigMapping> getMapping(String name) {
    return getMapping(ConfigMapping.Id.of(name));
  }

  @Override
  public Optional<ConfigMapping> getMapping(String name, String instance) {
    return getMapping(ConfigMapping.Id.of(name, instance));
  }

  @Override
  public Optional<ConfigMapping> getMapping(ConfigMapping.Id id) {
    return Optional.ofNullable(mappings.computeIfAbsent(id, this::newMapping))
        .filter(ConfigMappingImpl::isAvailable)
        .map(ConfigMapping.class::cast);
  }

  protected BundleContext getBundleContext() {
    final Bundle bundle = FrameworkUtil.getBundle(ConfigMappingServiceImpl.class);

    if (bundle != null) {
      return bundle.getBundleContext();
    }
    throw new IllegalStateException("missing bundle for ConfigMappingServiceImpl");
  }

  @Nullable
  private ConfigMappingImpl newMapping(ConfigMapping.Id id) {
    // search all registered providers to find those that supports the specified mapping
    return new ConfigMappingImpl(config, id, providers.stream().filter(p -> p.canProvideFor(id)));
  }

  private void bind(ConfigMappingInformation provider, ConfigMappingImpl mapping) {
    if (mapping.bind(provider)) {
      notifyUpdated(mapping);
    }
  }

  private void unbind(ConfigMappingInformation provider, ConfigMappingImpl mapping) {
    if (mapping.unbind(provider)) {
      if (mapping.isAvailable()) {
        notifyUpdated(mapping);
      } else {
        notifyRemoved(mapping);
      }
    }
  }

  private void updated(Map<String, Set<String>> ids) {
    LOGGER.debug("ConfigMappingServiceImpl::updated({})", ids);
    // mappings.values().stream().filter(m -> m.shouldBeUpdated(ids)).forEach(this::notifyUpdated);
  }

  //  private void updated(String id, File artifact) throws IOException {
  //    try {
  //      notifyUpdated(
  //          mappings.compute(
  //              id,
  //              (i, m) -> {
  //                if (m == null) {
  //                  m = new ConfigMappingImpl(agent, artifact);
  //                } else {
  //                  m.loadRules();
  //                }
  //                return m;
  //              }));
  //    } catch (UncheckedIOException e) {
  //      throw e.getCause();
  //    }
  //  }

  private void notifyUpdated(ConfigMapping mapping) {
    LOGGER.debug("ConfigMappingServiceImpl::notifyUpdated({})", mapping);
    listeners.forEach(l -> l.updated(mapping));
  }

  private void notifyRemoved(ConfigMapping mapping) {
    LOGGER.debug("ConfigMappingServiceImpl::notifyRemoved({})", mapping);
    listeners.forEach(l -> l.removed(mapping));
  }
}
