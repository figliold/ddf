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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFileHandler implements FileHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileHandler.class);

    public abstract Dictionary<String, Object> read(String file) throws IOException;

    public abstract void write(String file, Dictionary<String, Object> properties) throws IOException;
    
    public abstract String getFileNameForPid(String pid);
    
    public abstract String getExtension();
    
    @Override
    public boolean exists(String file) {
        return Files.exists(Paths.get(file));
    }

    @Override
    public void delete(String file) throws IOException {
        Files.delete(Paths.get(file));
    }

    @Override
    public List<String> listFiles(String directory) throws IOException {
        List<String> fileNames = new ArrayList<>();
        if (Files.isDirectory(Paths.get(directory))) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
                    Paths.get(directory), "*" + getExtension())) {
                for (Path path : directoryStream) {
                    fileNames.add(path.toString());
                }
            } catch (IOException e) {
                throw new IOException("Unable to list files with extension " + getExtension() + " in directory " + directory, e);
            }
        }
        return fileNames;
    }
}
