/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.configuration.store;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationMigrationManager implements ConfigurationMigrationService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConfigurationMigrationManager.class);

    private ConfigurationAdminMigrator configurationAdminMigrator;

    private SystemConfigurationMigrator systemConfigurationMigrator;

    public ConfigurationMigrationManager(ConfigurationAdminMigrator configurationAdminMigrator,
            SystemConfigurationMigrator systemConfigurationMigrator) {
        this.configurationAdminMigrator = configurationAdminMigrator;
        this.systemConfigurationMigrator = systemConfigurationMigrator;
    }

    @Override
    public void export(Path exportDirectory) throws ConfigurationFileException, IOException {
        this.configurationAdminMigrator.export(exportDirectory);
        this.systemConfigurationMigrator.export(exportDirectory);
    }
}
