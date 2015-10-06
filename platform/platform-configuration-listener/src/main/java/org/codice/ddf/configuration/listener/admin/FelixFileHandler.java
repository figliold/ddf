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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.felix.cm.file.ConfigurationHandler;
import org.codice.ddf.configuration.ConfigurationFileException;
import org.codice.ddf.configuration.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FelixFileHandler implements FileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FelixFileHandler.class);

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    private Map<String, Dictionary<String, Object>> propertiesCache = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public Dictionary<String, Object> read(String file) {
        Dictionary<String, Object> properties = null;
        readLock.lock();
        try (InputStream inputStream = new FileInputStream(file)) {
            properties = org.apache.felix.cm.file.ConfigurationHandler.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }

        return properties;
    }

    @Override
    public void write(String file, Dictionary<String, Object> properties) {
        writeLock.lock();
        try {
            if (!exists(file)) {
                LOGGER.debug(
                        "Did not find custom configuration file [{}]. The configuration will not be written.",
                        file);
                propertiesCache.remove(file);
                return;
            }
            if (doesPropertyNeedToBeWrittenToFile(file, properties)) {
                writeFile(file, properties);
                propertiesCache.put(file, properties);
            } else {
                LOGGER.debug(
                        "Cached properties for key [{}] are equal to the incoming properties...not updating configuration file [{}]...",
                        file, file);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void delete(String file) {
        writeLock.lock();
        try {
            if (!exists(file)) {
                LOGGER.debug(
                        "Did not find custom configuration file [{}]. The configuration will not be written out to [{}].",
                        file, file);
                propertiesCache.remove(file);
                return;
            }
            deleteFile(file);
            propertiesCache.remove(file);
        } finally {
            writeLock.unlock();
        }
    }

    private void deleteFile(String file) {
        try {
            LOGGER.debug("Deleting configuration file [{}].", file);
            Files.delete(Paths.get(file));
        } catch (IOException e) {
            throw new ConfigurationFileException("TODO"); // TODO
            // TODO
        }
    }

    private void writeFile(String file, Dictionary<String, Object> properties) {

        try (FileOutputStream out = new FileOutputStream(file);
                FileLock fileLock = out.getChannel().tryLock()) {
            if (fileLock == null) {
                throw new ConfigurationFileException("TODO"); // TODO
            }
            ConfigurationHandler.write(out, properties);
        } catch (IOException e) {
            throw new ConfigurationFileException("TODO"); // TODO
        }
    }

    public boolean exists(String file) {
        return Files.exists(Paths.get(file));
    }

    private boolean doesPropertyNeedToBeWrittenToFile(String file,
            Dictionary<String, Object> properties) {
        if (propertiesCache.containsKey(file)) {
            return !equal(propertiesCache.get(file), properties);
        }
        return true;
    }

    private boolean equal(Dictionary<String, Object> props1, Dictionary<String, Object> props2) {
        if (props1.size() != props2.size()) {
            return false;
        }

        for (Enumeration<String> keys = props1.keys(); keys.hasMoreElements(); ) {
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

}
