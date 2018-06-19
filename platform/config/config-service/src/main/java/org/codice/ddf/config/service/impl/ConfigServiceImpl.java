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
package org.codice.ddf.config.service.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.codice.ddf.config.Config;
import org.codice.ddf.config.ConfigInstance;
import org.codice.ddf.config.ConfigService;
import org.codice.ddf.config.reader.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigServiceImpl implements ConfigService, ArtifactInstaller {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceImpl.class);

  private final ConfigTracker configTracker = new ConfigTracker();

  private final ConfigReader configReader;

  public ConfigServiceImpl(ConfigReader configReader) {
    this.configReader = configReader;
    LOGGER.error("##### class name for config reader: " + this.configReader.getClass().getName());
    LOGGER.error("#### class loader: {}", this.configReader.getClass().getClassLoader());
  }

  @Override
  public <T extends Config> Optional<T> get(Class<T> key) {
    LOGGER.error("##### ConfigServiceImpl::get(key)");
    //    return Optional.ofNullable((T) current.get(key));
    return Optional.empty();
  }

  @Override
  public <T extends ConfigInstance> Optional<T> get(Class<T> key, String id) {
    LOGGER.error("##### ConfigServiceImpl::get(key, id)");
    //    Collection<Config> configs = current.get(key);
    //    Optional<ConfigInstance> configInstance =
    //        configs
    //            .stream()
    //            .filter(c -> c instanceof ConfigInstance)
    //            .map(c -> (ConfigInstance) c)
    //            .filter(c -> c.getId().equals(id))
    //            .findFirst();
    //    return (Optional<T>) configInstance;
    return Optional.empty();
  }

  @Override
  public void install(File config) throws Exception {
    LOGGER.error("##### Start ConfigServiceImpl::install(config)");
    Set<Config> configs = read(config);
    configTracker.updateCurrent(configs);
    Multimap<Class<?>, Config> updates = configTracker.computeUpdates();
    notifyListeners(updates);
    LOGGER.error("##### End ConfigServiceImpl::install(config)");
  }

  @Override
  public void update(File config) throws Exception {
    LOGGER.error("##### Start ConfigServiceImpl::update(config)");
    Set<Config> configs = read(config);
    configTracker.updateCurrent(configs);
    Multimap<Class<?>, Config> updates = configTracker.computeUpdates();
    notifyListeners(updates);
    LOGGER.error("##### End ConfigServiceImpl::update(config)");
  }

  @Override
  public void uninstall(File config) throws Exception {
    LOGGER.error("##### Start ConfigServiceImpl::uninstall(config)");
    LOGGER.error("##### End ConfigServiceImpl::uninstall(config)");
  }

  private void notifyListeners(Multimap<Class<?>, Config> configs) {
    LOGGER.error("##### End ConfigServiceImpl::notifyListeners(config)");
  }

  @Override
  public boolean canHandle(File config) {
    return config.getName().endsWith(".yml") || config.getName().endsWith(".yaml");
  }

  private Set<Config> read(File config) {
    try {
      return configReader.read(config);
    } catch (IOException e) {
      LOGGER.error("Unable to read configuration file: {}", config.getAbsoluteFile());
      return ImmutableSet.of();
    }
  }
}
