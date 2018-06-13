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

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.codice.ddf.config.mapping.ConfigMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Monitors directories on disk in order to locate configuration mapping rules stored on disk. */
public class OSGIConfigMappingFileMonitor extends ConfigMappingFileMonitor
    implements ArtifactInstaller {
  private static final Logger LOGGER = LoggerFactory.getLogger(OSGIConfigMappingFileMonitor.class);

  private final Set<Path> paths;

  public OSGIConfigMappingFileMonitor(ConfigMappingService mapper) {
    this(mapper, ImmutableSet.of(System.getProperty("ddf.home") + "/etc/mappings"));
  }

  public OSGIConfigMappingFileMonitor(ConfigMappingService mapper, Set<String> paths) {
    super(mapper);
    this.paths = paths.stream().map(File::new).map(File::toPath).collect(Collectors.toSet());
  }

  @SuppressWarnings("unused" /* called by blueprint */)
  public void init() {
    LOGGER.debug("OSGIConfigMappingFileMonitor::init()");
  }

  @Override
  public boolean canHandle(File file) {
    final Path path = file.toPath();
    final String name = file.getName();

    return name.endsWith(".json") && paths.stream().anyMatch(path::startsWith);
  }
}
