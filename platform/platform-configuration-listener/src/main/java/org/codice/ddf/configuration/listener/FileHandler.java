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
import java.util.Dictionary;
import java.util.List;

public interface FileHandler {
    
    public Dictionary<String, Object> read(String file) throws IOException;
    
    public void write(String file, Dictionary<String, Object> properties) throws IOException;
    
    public void delete(String file) throws IOException;
    
    public boolean exists(String file);
    
    public List<String> listFiles(String directory) throws IOException;
    
    public String getFileNameForPid(String pid);

}
