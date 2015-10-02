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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FelixFileHandler extends AbstractFileHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FelixFileHandler.class);
    
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    
    private final Lock readLock = readWriteLock.readLock();
    
    private final Lock writeLock = readWriteLock.writeLock();
    
    private static final String EXT = ".config";

    @SuppressWarnings("unchecked")
    @Override
    public Dictionary<String, Object> read(String file) throws IOException {
        Dictionary<String, Object> properties = null;
        writeLock.lock();
        try (InputStream inputStream = new FileInputStream(file)) {
            properties = org.apache.felix.cm.file.ConfigurationHandler.read(inputStream);
        } finally {
            writeLock.unlock();
        }

        return properties;
    }

    @Override
    public void write(String file, Dictionary<String, Object> properties) throws IOException {
        readLock.lock();
        try (OutputStream outputStream = new FileOutputStream(file)) {
            org.apache.felix.cm.file.ConfigurationHandler.write(outputStream, properties);
        } finally {
            readLock.unlock();
        }
    }
    
    @Override
    public String getFileNameForPid(String pid) {
        return pid + EXT;
    }
    
    @Override
    public String getExtension() {
        return EXT;
    }
}
