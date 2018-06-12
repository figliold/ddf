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
package org.codice.ddf.config.osgi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.apache.commons.lang.ArrayUtils;
import org.codice.ddf.config.mapping.ConfigMapping;
import org.codice.ddf.config.mapping.ConfigMappingException;
import org.codice.ddf.config.mapping.ConfigMappingListener;
import org.codice.ddf.config.mapping.ConfigMappingService;
import org.codice.ddf.configuration.DictionaryMap;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.SynchronousConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigAdminAgent implements SynchronousConfigurationListener, ConfigMappingListener {
  public static final String INSTANCE_KEY = "org.codice.ddf.config.instance";

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigAdminAgent.class);

  private final ConfigurationAdmin configAdmin;

  private final ConfigMappingService mapper;

  private final Map<String, Dictionary<String, Object>> cache = new ConcurrentHashMap<>();

  public ConfigAdminAgent(ConfigurationAdmin configAdmin, ConfigMappingService mapper) {
    this.configAdmin = configAdmin;
    this.mapper = mapper;
  }

  @SuppressWarnings("unused" /* called by blueprint */)
  public void init() {
    LOGGER.debug("ConfigAdminAgent:init()");
    try {
      configurations().forEach(this::updated);
    } catch (InvalidSyntaxException
        | ConfigMappingException
        | IOException
        | UncheckedIOException e) { // ignore
      LOGGER.debug("failed to initialize existing configurations: {}", e, e);
    }
  }

  @Override
  public void configurationEvent(ConfigurationEvent event) {
    LOGGER.debug("ConfigAdminAgent:configurationEvent({})", event);
    try {
      switch (event.getType()) {
        case ConfigurationEvent.CM_UPDATED:
        case ConfigurationEvent.CM_LOCATION_CHANGED:
          final ConfigurationAdmin cfgAdmin = getService(event.getReference());

          getConfiguration(cfgAdmin, event.getPid()).ifPresent(this::updated);
          break;
        case ConfigurationEvent.CM_DELETED:
          cache.remove(event.getPid());
          return;
        default:
          return;
      }
    } catch (InvalidSyntaxException
        | ConfigMappingException
        | IOException
        | UncheckedIOException e) { // ignore
      LOGGER.debug("failed to process configuration event: {}", e, e);
    }
  }

  @Override
  public void created(ConfigMapping mapping) {
    LOGGER.debug("ConfigAdminAgent:created({})", mapping);
    createdOrUpdated(mapping);
  }

  @Override
  public void updated(ConfigMapping mapping) {
    LOGGER.debug("ConfigAdminAgent:updated({})", mapping);
    createdOrUpdated(mapping);
  }

  @Override
  public void removed(ConfigMapping mapping) {
    LOGGER.debug("ConfigAdminAgent:removed({})", mapping);
    // just leave it alone - should we delete the corresponding config object???
  }

  BundleContext getBundleContext() {
    final Bundle bundle = FrameworkUtil.getBundle(ConfigAdminAgent.class);

    if (bundle != null) {
      return bundle.getBundleContext();
    }
    throw new IllegalStateException("missing bundle for ConfigAdminAgent");
  }

  private void createdOrUpdated(ConfigMapping mapping) {
    try {
      final Configuration cfg;
      final String pid = mapping.getId().getName();
      final String instance = mapping.getId().getInstance().orElse(null);

      if (instance != null) { // a managed service factory
        final Configuration[] cfgs =
            configAdmin.listConfigurations(
                String.format(
                    "(&(service.factoryPid=%s)(%s=%s))",
                    pid,
                    ConfigAdminAgent.INSTANCE_KEY,
                    ConfigAdminAgent.escapeFilterValue(instance)));

        if (ArrayUtils.isNotEmpty(cfgs)) {
          cfg = cfgs[0];
        } else {
          // get or create the first version
          // location as null to make sure it is bound to the first bundles that registers the
          // managed service factory
          cfg = configAdmin.createFactoryConfiguration(pid, null);
          LOGGER.debug(
              "created a new managed service factory for '{}-{}' as {}",
              pid,
              instance,
              cfg.getPid());
        }
      } else {
        // get or create the first version
        // location as null to make sure it is bound to the first bundles that registers the
        // managed service
        cfg = configAdmin.getConfiguration(pid, null);
      }
      updated(cfg, mapping);
    } catch (InvalidSyntaxException
        | ConfigMappingException
        | IOException
        | UncheckedIOException e) {
      LOGGER.debug("failed to process mapping update: {}", e, e);
    }
  }

  private void updated(Configuration cfg) {
    LOGGER.debug("ConfigAdminAgent:updated({})", cfg);
    final String factoryPid = cfg.getFactoryPid();
    final Optional<ConfigMapping> mapping;
    final String pid = cfg.getPid();

    if (factoryPid != null) { // see if we know its instance id
      final String instance =
          Objects.toString(cfg.getProperties().get(ConfigAdminAgent.INSTANCE_KEY), null);

      if (instance == null) {
        // we cannot handle that one specifically, check if we have mappings for the factory and
        // if we do, log this as an error since we should have an instance for the factories we
        // handled
        if (mapper.getMapping(factoryPid).isPresent()) {
          LOGGER.error(
              "unable to map managed service factory [{}]; missing instance from config object [{}]",
              factoryPid,
              pid);
        } else {
          LOGGER.debug(
              "unknown managed service factory; missing instance from config object [{}]", pid);
        }
        return;
      } else {
        LOGGER.debug(
            "found instance id from config object [{}]; handling it as [{}-{}]",
            pid,
            factoryPid,
            instance);
        mapping = mapper.getMapping(factoryPid, instance);
      }
    } else {
      mapping = mapper.getMapping(pid);
    }
    mapping.ifPresent(m -> updated(cfg, m));
  }

  private void updated(Configuration cfg, ConfigMapping mapping) {
    LOGGER.debug("ConfigAdminAgent:updated({}, {})", cfg, mapping);
    final Dictionary<String, Object> properties = ConfigAdminAgent.getProperties(cfg);
    final String instance = mapping.getId().getInstance().orElse(null);

    // compute the new mapping values
    mapping.resolve().forEach(properties::put);
    // keep the instance up to date
    if (instance != null) {
      properties.put(ConfigAdminAgent.INSTANCE_KEY, instance);
    } else {
      properties.remove(ConfigAdminAgent.INSTANCE_KEY);
    }
    // only update configAdmin if the dictionary content has changed
    final String pid = cfg.getPid();
    final Dictionary<String, Object> cachedProperties = cache.get(pid);

    if ((cachedProperties == null) || !ConfigAdminAgent.equals(cachedProperties, properties)) {
      LOGGER.debug("updating config [{}] in configAdmin with: {}", pid, properties);
      Dictionary<String, Object> old = null;

      try {
        old = cache.put(pid, properties);
        cfg.update(properties);
      } catch (IOException e) {
        if (old == null) {
          cache.remove(pid);
        } else {
          cache.put(pid, old);
        }
        throw new UncheckedIOException(e);
      }
    }
  }

  private Optional<Configuration> getConfiguration(ConfigurationAdmin configAdmin, String pid)
      throws InvalidSyntaxException, IOException {
    // we use listConfigurations to not bind the config object to our bundle if it was bound yet
    // as we want to make sure that it will be bounded to its corresponding service
    final String filter = String.format("(%s=%s)", org.osgi.framework.Constants.SERVICE_PID, pid);
    final Configuration[] configs = configAdmin.listConfigurations(filter);

    return ArrayUtils.isNotEmpty(configs) ? Optional.of(configs[0]) : Optional.empty();
  }

  private Stream<Configuration> configurations() throws InvalidSyntaxException, IOException {
    final Configuration[] configurations = configAdmin.listConfigurations(null);

    return (configurations != null) ? Stream.of(configurations) : Stream.empty();
  }

  private <S> S getService(ServiceReference<S> serviceReference) {
    return AccessController.doPrivileged(
        (PrivilegedAction<S>) () -> getBundleContext().getService(serviceReference));
  }

  private static Dictionary<String, Object> getProperties(Configuration cfg) {
    final Dictionary<String, Object> properties = cfg.getProperties();

    return (properties != null) ? properties : new DictionaryMap<>();
  }

  private static boolean equals(Dictionary<String, Object> x, Dictionary<String, Object> y) {
    if (x.size() != y.size()) {
      return false;
    }
    for (final Enumeration<String> e = x.keys(); e.hasMoreElements(); ) {
      final String key = e.nextElement();

      if (!Objects.deepEquals(x.get(key), y.get(key))) {
        return false;
      }
    }
    return true;
  }

  private static String escapeFilterValue(String s) {
    return s.replaceAll("[(]", "\\\\(")
        .replaceAll("[)]", "\\\\)")
        .replaceAll("[=]", "\\\\=")
        .replaceAll("[\\*]", "\\\\*");
  }
}
