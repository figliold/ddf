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
package org.codice.ddf.configuration.listener;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
    
    private final String configurationDirectory;

    private BundleContext bundleContext;
    
    private FileHandler fileHandler;
    
    private Map<String, Dictionary<String, Object>> propertiesCache = new HashMap<>();

    public ConfigurationHandler(BundleContext bundleContext, FileHandler fileHandler, String ddfHome) {
        this.bundleContext = bundleContext;
        configurationDirectory = ddfHome + File.separator + "etc";
        LOGGER.info("Configuration directory for [{}] is [{}].", ConfigurationHandler.class.getName(), configurationDirectory);
        this.fileHandler = fileHandler;
        LOGGER.debug("{} is using the {} file handler.", ConfigurationHandler.class.getName(), fileHandler.getClass().getName());
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
            handleCmUpdatedEvent(pid, file, properties);
            break;
        case ConfigurationEvent.CM_DELETED:
            handleCmDeletedEvent(pid, file);
            break;
        case ConfigurationEvent.CM_LOCATION_CHANGED:
            handleCmLocationChangedEvent(pid);
            break;
        default:
            return;
        }
    }
    
    private void handleCmUpdatedEvent(String pid, String file,
            Dictionary<String, Object> incomingProperties) {
        LOGGER.debug("Start Configuration Update Event.");
        if (exists(file) && arePropertiesCached(file)) {
            LOGGER.debug("Getting properties from properties cache for using key [{}]", file);
            Dictionary<String, Object> propertiesFromCache = getPropertiesFromCache(file);
            if (!equal(propertiesFromCache, incomingProperties)) {
                LOGGER.debug(
                        "Cached properties for key [{}] do not match incoming properties...updating configuration file [{}]...",
                        file, file);
                update(pid, file, incomingProperties);
            } else {
                LOGGER.debug(
                        "Cached properties for key [{}] are equal to the incoming properties...not updating configuration file [{}]...",
                        file, file);
            }
        } else if (exists(file) && !arePropertiesCached(file)) {
            LOGGER.debug("Unable to find properties in cache using key [{}]. Getting properties from configuration file [{}] instead.", file, file);
            Dictionary<String, Object> propertiesFromFile = read(pid, file);
            if (propertiesFromFile != null) {
                if (!equal(propertiesFromFile, incomingProperties)) {
                    LOGGER.debug(
                            "Properties from configuration file [{}] do not match incoming properties...updating configuration file [{}]...",
                            file, file);
                    update(pid, file, incomingProperties);
                } else {
                    LOGGER.debug("No cached properties for key [{}]...updating properties cache...", file);
                    addToCache(file, incomingProperties);
                    LOGGER.debug(
                            "Properties from configuration file [{}] are equal to the incoming properties...not updating configuration file [{}]...",
                            file, file);
                }
            }
        } else {
            LOGGER.debug(
                    "Did not find custom configuration file [{}]. The configuration for bundle with pid [{}] will not be written out to [{}].",
                    file, pid, file);
        }
        LOGGER.debug("End Configuration Update Event.");
    }
    
    private void addToCache(String file, Dictionary<String, Object> properties) {
        propertiesCache.put(file, properties);
    }
    
    private boolean arePropertiesCached(String file) {
        return propertiesCache.containsKey(file);
    }
    
    private Dictionary<String, Object> getPropertiesFromCache(String file) {
        return propertiesCache.get(file);
    }
    
    private boolean equal(Dictionary<String, Object> props1, Dictionary<String, Object> props2) {
        if (props1.size() != props2.size()) {
            return false;
        }

        for (Enumeration<String> keys = props1.keys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            Object value1 = props1.get(key);
            Object value2 = props2.get(key);
            if (value2 == null) {
                return false;
            } 
            if (value1 instanceof Object[] && value2 instanceof Object[]) {
                if (!Arrays.equals((Object[]) value1, (Object[]) value2)) {
                    return false;
                }
            } else if (!value1.equals(value2)) {
                return false;
            }
        }
        return true;
    }
    
    private String getFileName(String pid) {
        return configurationDirectory + File.separator + fileHandler.getFileNameForPid(pid);
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
        LOGGER.debug("Writing properties [{}] to configuraiton file [{}]", properties, file);
        try {
            write(file, properties);
            LOGGER.debug("Updating properties cache using key [{}]", file);
            addToCache(file, properties);
        } catch (IOException e) {
            LOGGER.error("Unable to write properties for pid [{}] to configuration file [{}].",
                    pid, file);
        }
    }

    private void handleCmDeletedEvent(String pid, String file) {
        LOGGER.debug("Deleting configuration file [{}] for bundle with pid [{}].", file, pid);
        try {
            fileHandler.delete(file);
        } catch (IOException e) {
            LOGGER.error("Unable to delete configuration file [{}].", file, e);
        }
    }

    private void handleCmLocationChangedEvent(String pid) {
        LOGGER.info("Unsupported configuration event of CM Location Changed. No action taken for pid [{}]", pid);
    }

    private void write(String file, Dictionary<String, Object> properties) throws IOException {
        fileHandler.write(file, properties);
    }
    
    private Dictionary<String, Object> read(String pid, String file) {
        try {
            return fileHandler.read(file);
        } catch (IOException e) {
            LOGGER.error("Unable to read properties for pid [{}] from file [{}].", pid, file);
            return null;
        }
    }
        
    private boolean exists(String file) {
        return fileHandler.exists(file);
    }
}
