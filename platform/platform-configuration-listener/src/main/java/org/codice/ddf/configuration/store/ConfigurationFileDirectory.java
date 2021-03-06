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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that provides utility methods to access the configuration files in the configuration
 * directory.
 */
public class ConfigurationFileDirectory implements ChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationFileDirectory.class);

    private DirectoryStream<Path> directoryStream;

    private Path processedDirectory;

    private Path failedDirectory;

    private ConfigurationFileFactory configurationFileFactory;

    private ConfigurationFilesPoller poller;

    /**
     * Constructor.
     *
     * @param directoryStream          reference to a {@link DirectoryStream} that will return the
     *                                 configuration files to read in upon startup
     * @param processedDirectory       directory where configuration files will be moved to after
     *                                 being successfully processed
     * @param failedDirectory          directory where configuration files will be moved when they
     *                                 failed to be processed
     * @param configurationFileFactory factory object used to create {@link ConfigurationFile}
     *                                 instances based on their type
     * @param poller                   object to register with to know when new configuration
     *                                 files have been created
     * @throws IllegalArgumentException thrown if any of the arguments is invalid
     */
    public ConfigurationFileDirectory(@NotNull DirectoryStream directoryStream,
            @NotNull Path processedDirectory, @NotNull Path failedDirectory,
            @NotNull ConfigurationFileFactory configurationFileFactory,
            @NotNull ConfigurationFilesPoller poller) {

        notNull(directoryStream, "Directory stream cannot be null");
        notNull(processedDirectory, "Processed directory cannot be null");
        notNull(failedDirectory, "Failed directory cannot be null");
        notNull(configurationFileFactory, "Configuration file factory cannot be null");
        notNull(poller, "Directory poller cannot be null");

        this.directoryStream = directoryStream;
        this.processedDirectory = processedDirectory;
        this.failedDirectory = failedDirectory;
        this.configurationFileFactory = configurationFileFactory;
        this.poller = poller;
    }

    /**
     * Loads all the configuration files located in the configuration directory specified in the
     * constructor. Files that have been successfully processed will be moved to the
     * {@code processedDirectory} and files that failed to be processed will be moved to the
     * {@code failedDirectory}. It will also register for file creation events which will
     * be handled by the {@link #notify(Path)} method.
     *
     * @throws IOException thrown if an I/O error occurs during initialization
     */
    public void init() throws IOException {
        createDirectory(processedDirectory);
        createDirectory(failedDirectory);

        Collection<ConfigurationFile> configFiles = getConfigurationFiles();

        for (ConfigurationFile configFile : configFiles) {
            try {
                configFile.createConfig();
                moveConfigurationFile(configFile.getConfigFilePath(), processedDirectory);
            } catch (ConfigurationFileException e) {
                moveConfigurationFile(configFile.getConfigFilePath(), failedDirectory);
            }
        }

        LOGGER.debug("Registering with [{}] for directory changes.", poller.getClass().getName());
        poller.register(this);
    }

    @Override
    public void notify(Path file) {
        try {
            ConfigurationFile configFile = configurationFileFactory.createConfigurationFile(file);
            configFile.createConfig();
            moveConfigurationFile(file, processedDirectory);
        } catch (ConfigurationFileException e) {
            moveConfigurationFile(file, failedDirectory);
        }
    }

    void moveFile(Path source, Path destination) throws IOException {
        Files.move(source, destination.resolve(source.getFileName()), REPLACE_EXISTING);
    }

    private void moveConfigurationFile(Path source, Path destination) {
        try {
            moveFile(source, destination);
        } catch (IOException e) {
            LOGGER.warn(String.format("Failed to move %s to %s directory", source.toString(),
                    destination.toString()), e);
        }
    }

    private Collection<ConfigurationFile> getConfigurationFiles() throws IOException {
        Collection<Path> files = listFiles();
        Collection<ConfigurationFile> configurationFiles = new ArrayList<>(files.size());

        for (Path file : files) {
            ConfigurationFile configFile;

            try {
                configFile = configurationFileFactory.createConfigurationFile(file);
                configurationFiles.add(configFile);
            } catch (ConfigurationFileException e) {
                LOGGER.error(e.getMessage(), e);
                moveConfigurationFile(file, failedDirectory);
            }
        }

        return configurationFiles;
    }

    private Collection<Path> listFiles() throws IOException {
        Collection<Path> fileNames = new ArrayList<>();

        for (Path path : directoryStream) {
            fileNames.add(path);
        }

        return fileNames;
    }

    private void createDirectory(Path path) throws IOException {
        File file = path.toFile();

        if (file.exists()) {
            if (!file.isDirectory()) {
                LOGGER.error(
                        "Failed to create directory [{}]. A file with the same name already exists.",
                        path.toString());
                throw new IOException(String.format(
                        "Failed to create %s. A file with the same name already exists.",
                        path.toString()));
            }
        } else {
            if (!file.mkdir()) {
                LOGGER.error("Failed to create directory [{}].", path.toString());
                throw new IOException(String.format("Failed to create %s.", path.toString()));
            }
        }
    }
}
