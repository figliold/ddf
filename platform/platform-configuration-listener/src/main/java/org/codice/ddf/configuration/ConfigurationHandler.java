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
package org.codice.ddf.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Dictionary;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationHandler implements ConfigurationListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationHandler.class);
    
    private static final String EXT = ".config";
    
    private final String configurationDirectory;

    private BundleContext bundleContext;
    
    public ConfigurationHandler(BundleContext bundleContext, String ddfHome) {
        this.bundleContext = bundleContext;
        configurationDirectory = ddfHome + File.separator + "etc";
        LOGGER.info("Configuration directory for [{}] is [{}].", ConfigurationHandler.class.getName(), configurationDirectory);
    }

    @Override
    public void configurationEvent(ConfigurationEvent event) {
        String pid = event.getPid();
        String factoryPid = event.getFactoryPid();
        LOGGER.debug("factory pid: {}", factoryPid);
        int eventType = event.getType();
        LOGGER.debug("event type: {}", eventType);
        LOGGER.debug("Received configuration event for bundle with pid [{}].", pid);
        Configuration configuration = getConfiguration(pid, event);
        Dictionary<String, Object> properties = configuration.getProperties();
        String file = getFileName(pid);
        LOGGER.debug("file: {}", file);
        switch (eventType) {
        case ConfigurationEvent.CM_UPDATED:
            LOGGER.debug("Configuration update event.");
            if (exists(file)) {
                update(pid, file, properties);
            } else {
                LOGGER.debug("Did not find custom configuration file [{}]. The configuration for bundle with pid [{}] will not be written out to [{}].", file, pid, file);
            }
            break;
        case ConfigurationEvent.CM_DELETED:
            LOGGER.debug("Configuration deleted event.");
            delete(pid, file);
            break;
        case ConfigurationEvent.CM_LOCATION_CHANGED:
            LOGGER.debug("Configuration location changed event.");
            move(pid, file);
            break;
        default:
            return;
        }
    }
    
    private String getFileName(String pid) {
        return configurationDirectory + File.separator + pid + EXT;
    }
    
    private Configuration getConfiguration(String pid, ConfigurationEvent event) {
        ServiceReference<ConfigurationAdmin> configAdminServiceReference = event.getReference();
        ConfigurationAdmin configAdmin = bundleContext.getService(configAdminServiceReference);
        Configuration configuration = null;
        try {
            configuration = configAdmin.getConfiguration(pid);
        } catch (IOException e) {
            LOGGER.error("Unable to get the configuration for pid [{}] from ConfigurationAdmin", pid, e);
        }
        
        return configuration;
    }

    private void update(String pid, String file, Dictionary<String, Object> properties) {
        LOGGER.debug("writing properties [{}] to file [{}]", properties, file);
        write(pid, file, properties);
    }

    private void delete(String pid, String file) {
        LOGGER.error("Deleting configuration file file [{}] for bundle with pid [{}].", file, pid);
    }

    private void move(String pid, String file) {
        LOGGER.error("Moving configuration file [{}] for bundle with pid [{}].", file, pid);
    }

    private void write(String pid, String file, Dictionary<String, Object> properties) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            org.apache.felix.cm.file.ConfigurationHandler.write(outputStream, properties);
        } catch (IOException e) {
            LOGGER.error("Unable to write properties for pid [{}] to configuration file [{}].", pid, file);
        }
    }
        
    private boolean exists(String file) {
        return new File(file).exists();
    }
}
