package org.codice.ddf.configuration;

public class ConfigurationFileException extends RuntimeException {

    public ConfigurationFileException(String message){
        super(message);
    }

    public ConfigurationFileException(String message, Throwable cause){
        super(message, cause);
    }
}
