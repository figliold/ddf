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
package org.codice.ddf.configuration.listener.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Dictionary;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.codice.ddf.configuration.FileHandler;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationFileListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationFileListener.class);

    private final WatchService watchService;

    private final ExecutorService watchThread;

    private final Path configurationDirectory;

    private final FileHandler fileHandler;

    private final ConfigurationAdmin configAdmin;

    private final String fileExtension;

    public ConfigurationFileListener(String configurationDirectory, WatchService watchService,
            ExecutorService watchThread, FileHandler fileHandler, ConfigurationAdmin configAdmin,
            String fileExtension) throws IOException {
        this.configurationDirectory = Paths.get(configurationDirectory);
        this.watchService = watchService;
        this.watchThread = watchThread;
        this.fileHandler = fileHandler;
        this.configAdmin = configAdmin;
        this.fileExtension = fileExtension;
        this.configurationDirectory
                .register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        LOGGER.debug(
                "Configuration directory for [{}] is [{}].  Files with an extension of [{}] will be observed.",
                ConfigurationFileListener.class.getName(), configurationDirectory, fileExtension);
    }

    /**
     * Iterates over the entire configuration directory at the beginning to push all the files through ConfigAdmin
     */
    public void init() {
        LOGGER.debug(
                "Configuration Observer is initializing by reading/updating all the configurations in [{}] with the file extension of [{}]",
                this.configurationDirectory, this.fileExtension);
        for (File file : new File(configurationDirectory.toString())
                .listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(fileExtension);
                    }
                })) {
            updateConfig(file);
        }
        LOGGER.debug(
                "Starting the Configuration Observer.  From this point onward, any updates to files with the extension of [{}] in [{}] will be detected and acted on.",
                this.fileExtension, this.configurationDirectory);
        watchThread.execute(new ConfigurationPoller());
    }

    /**
     * Attempts to allow ConfigurationPoller the opportunity to gracefully shutdown.
     */
    public void destroy() {
        watchThread.shutdown();
        try {
            if (!watchThread.awaitTermination(10, TimeUnit.SECONDS)) {
                watchThread.shutdownNow();
                if (!watchThread.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOGGER.error("[{}] did not terminate correctly.",
                            ConfigurationPoller.class.getName());
                }
            }
        } catch (InterruptedException ex) {
            watchThread.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void updateConfig(File file, String pid) {
        Dictionary<String, Object> props;
        props = fileHandler.read(file.getAbsolutePath());
        LOGGER.debug("Updating [{}] configuration based off [{}].", pid, file.getName());
        try {
            Configuration configuration = configAdmin.getConfiguration(pid, null);
            configuration.update(props);
        } catch (IOException ex) {
            LOGGER.error("[{}] configuration could not be found, or failed to update.", pid, ex);
            return;
        }
    }

    private void updateConfig(File file) {
        String pid = file.getName().substring(0, file.getName().lastIndexOf("."));
        updateConfig(file, pid);
    }

    private void deleteConfig(String pid) {
        try {
            configAdmin.getConfiguration(pid, null).delete();
            LOGGER.debug("[{}] was deleted successfully.", pid);
        } catch (IOException ex) {
            LOGGER.error("There was an issue deleting [{}].", pid, ex);
        }
    }

    private class ConfigurationPoller implements Runnable {
        @Override
        public void run() {
            try {
                WatchKey key;
                while (!Thread.currentThread().isInterrupted()) {
                    key = watchService.take();  //blocking
                    LOGGER.debug("Key has been signalled.  Looping over events.");

                    for (WatchEvent<?> genericEvent : key.pollEvents()) {
                        String kind = genericEvent.kind().name();
                        String filename = ((WatchEvent<Path>) genericEvent).context().toString();

                        if (kind.equals("OVERFLOW") || !filename.endsWith(fileExtension)) {
                            LOGGER.debug(
                                    "Skipping event for [{}] due to overflow or file extension.",
                                    filename);
                            continue;  //just skip to the next event
                        }
                        LOGGER.debug("Processing [{}] event for for [{}].", kind, filename);

                        // I tried to use event.context().toAbsolutePath for pathToFile, but it gave the wrong path (it was missing the etc dir)
                        String pathToFile = configurationDirectory + File.separator + filename;
                        String pid = filename.substring(0, filename.lastIndexOf("."));
                        try {
                            switch (kind) {
                            case "ENTRY_CREATE":
                            case "ENTRY_MODIFY":
                                updateConfig(new File(pathToFile), pid);
                                break;
                            case "ENTRY_DELETE":
                                deleteConfig(pid);
                                break;
                            }
                        } catch (RuntimeException e) {
                            // TODO
                        }
                    }
                    // reset key, shutdown watcher if directory no able to be observed (possibly deleted, who knows)
                    if (!key.reset()) {
                        LOGGER.warn("Configurations in [{}] are no longer able to be observed.",
                                configurationDirectory);
                        break;
                    }
                }
            } catch (InterruptedException ex) {
                LOGGER.error("The Configuration Observer was interrupted.", ex);
                Thread.currentThread().interrupt();
            }
        }
    }

}
