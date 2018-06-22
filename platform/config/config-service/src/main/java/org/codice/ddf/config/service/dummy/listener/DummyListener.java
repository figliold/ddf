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
package org.codice.ddf.config.service.dummy.listener;

import java.util.Optional;
import org.codice.ddf.config.ConfigEvent;
import org.codice.ddf.config.ConfigListener;
import org.codice.ddf.config.ConfigService;
import org.codice.ddf.config.model.CswFederationProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyListener implements ConfigListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(DummyListener.class);

  private ConfigService configService;

  public DummyListener(ConfigService configService) {
    this.configService = configService;
  }

  @Override
  public void configChanged(ConfigEvent event) {
    System.out.println("Added Configs");
    event.addedConfigs().forEach(System.out::println);
    System.out.println("Updated Configs");
    event.updatedConfigs().forEach(System.out::println);
    System.out.println("Removed Configs");
    event.removedConfigs().forEach(System.out::println);

    LOGGER.error("##### Performing get()");
    Optional<CswFederationProfileConfig> configOptional =
        configService.get(CswFederationProfileConfig.class, "csw1");
    if (configOptional.isPresent()) {
      CswFederationProfileConfig config = configOptional.get();
      LOGGER.error("##### Config received from get(): {}", config);
    }
  }
}
