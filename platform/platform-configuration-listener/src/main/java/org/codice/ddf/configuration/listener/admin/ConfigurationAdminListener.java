/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.configuration.listener.admin;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;

import org.codice.ddf.configuration.FileHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationAdminListener implements ConfigurationListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationAdminListener.class);

    private static final String EXT = ".config";

    private final String configurationDirectory;

    private BundleContext bundleContext;

    private FileHandler fileHandler;

    public ConfigurationAdminListener(BundleContext bundleContext, FileHandler fileHandler,
            String ddfHome) {
        this.bundleContext = bundleContext;
        configurationDirectory = ddfHome + File.separator + "etc";
        LOGGER.info("Configuration directory for [{}] is [{}].",
                ConfigurationAdminListener.class.getName(), configurationDirectory);
        this.fileHandler = fileHandler;
        LOGGER.debug("{} is using the {} file handler.", ConfigurationAdminListener.class.getName(),
                fileHandler.getClass().getName());
    }

    @Override
    public void configurationEvent(ConfigurationEvent event) {
        String pid = event.getPid();
        int eventType = event.getType();
        String factoryPid = event.getFactoryPid();
        LOGGER.debug("Factory pid: {}", factoryPid);
        LOGGER.debug("Event type: {}", eventType);
        LOGGER.debug("Received configuration event for bundle with pid [{}].", pid);
        try {
            Configuration configuration = getConfiguration(pid, event);
            Dictionary<String, Object> properties = configuration.getProperties();
            String file = getFileName(pid);
            LOGGER.debug("file: {}", file);
            switch (eventType) {
            case ConfigurationEvent.CM_UPDATED:
                fileHandler.write(file, properties);
                break;
            case ConfigurationEvent.CM_DELETED:
                fileHandler.delete(file);
                break;
            default:
                LOGGER.info(
                        "Unsupported ConfigurationEvent [{}]. No action taken for pid [{}].",
                        eventType, pid);
            }
        } catch (RuntimeException | IOException e) {
            LOGGER.error("Failed to process ConfigurationEvent [{}] for pid [{}].",
                    eventType, pid, e);
        }
    }

    private String getFileName(String pid) {
        return configurationDirectory + File.separator + pid + EXT;
    }

    private Configuration getConfiguration(String pid, ConfigurationEvent event)
            throws IOException {
        ServiceReference<ConfigurationAdmin> configAdminServiceReference = event.getReference();
        ConfigurationAdmin configAdmin = bundleContext.getService(configAdminServiceReference);
        return configAdmin.getConfiguration(pid, null);
    }
}
