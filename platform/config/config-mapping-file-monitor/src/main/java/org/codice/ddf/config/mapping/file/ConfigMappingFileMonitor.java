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
package org.codice.ddf.config.mapping.file;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.codice.ddf.config.mapping.ConfigMappingProvider;
import org.codice.ddf.config.mapping.ConfigMappingService;
import org.codice.ddf.config.mapping.groovy.GroovyConfigMappingReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Monitors directories on disk in order to locate configuration mapping rules stored on disk. */
public class ConfigMappingFileMonitor implements Closeable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMappingFileMonitor.class);

  private final ConfigMappingService mapper;

  private final Map<File, ConfigMappingProvider> providers = new HashMap<>();

  private final GroovyConfigMappingReader reader = new GroovyConfigMappingReader();

  private final Object lock = new Object();

  public ConfigMappingFileMonitor(ConfigMappingService mapper) {
    this.mapper = mapper;
  }

  @Override
  public void close() {
    LOGGER.debug("ConfigMappingFileMonitor::close()");
    // unbinds all providers
    synchronized (lock) {
      providers.values().forEach(mapper::unbind);
      providers.clear();
    }
  }

  public void install(File file) {
    LOGGER.debug("ConfigMappingFileMonitor::install({})", file);
    final ConfigMappingProvider provider = loadProvider(file);

    if (provider == null) { // fail to load
      return;
    }
    synchronized (lock) {
      final ConfigMappingProvider old = providers.put(file, provider);

      if (old != null) { // oops! one was already registered so keep that one
        LOGGER.debug("config mapping provider for [{}] already exist", file);
        providers.put(file, old);
      } else {
        LOGGER.debug("binding [{}] config mapping provider: {}", file, provider);
        mapper.bind(provider);
      }
    }
  }

  public void update(File file) {
    LOGGER.debug("ConfigMappingFileMonitor::update({})", file);
    final ConfigMappingProvider provider = loadProvider(file);

    if (provider == null) { // fail to load
      return;
    }
    synchronized (lock) {
      final ConfigMappingProvider old = providers.put(file, provider);

      // we need to first unbind the existing provider if any an the rebind it
      if (old != null) {
        LOGGER.debug("rebinding [{}] config mapping provider [{}]: {}", file, old, provider);
        mapper.unbind(old);
      } else {
        LOGGER.debug("binding [{}] config mapping provider: {}", file, provider);
      }
      mapper.bind(provider);
    }
  }

  public void uninstall(File file) {
    LOGGER.debug("ConfigMappingFileMonitor::uninstall({})", file);
    synchronized (lock) {
      final ConfigMappingProvider provider = providers.remove(file);

      if (provider != null) {
        LOGGER.debug("unbinding [{}] config mapping provider: {}", file, provider);
        mapper.unbind(provider);
      } else {
        LOGGER.debug("no config mapping provider found for file: {}", file);
      }
    }
  }

  @Nullable
  private ConfigMappingProvider loadProvider(File file) {
    try {
      return reader.parse(file);
    } catch (IOException e) {
      LOGGER.error("failed to load config mapping file '{}': {}", file, e.getMessage());
      LOGGER.debug("loading failure: {}", e, e);
      return null;
    }
  }
}
