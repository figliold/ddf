/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigServiceImpl implements ConfigService {

  //    private final Reader yamlReader;
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceImpl.class);

  private final Multimap<Class<?>, Config> previous = HashMultimap.create();

  private final Multimap<Class<?>, Config> current = HashMultimap.create();

  //    public ConfigServiceImpl(Reader reader) {
  //        this.yamlReader = reader;
  //    }

  public ConfigServiceImpl() {
    LOGGER.error("##### ConfigServiceImpl::ctor()");
    current.put(
        MimeTypeConfig.class,
        new SchemaMimeTypeConfigImpl(
            "csw",
            "CSW Content Resolver",
            24,
            "http://www.opengis.net/cat/csw/2.0.2",
            ".xml",
            "text/xml;id=csw"));
    current.put(
        MimeTypeConfig.class,
        new MimeTypeConfigImpl(
            "geojson", "GeoJson Content Resolver", 20, ".json", "application/json;id=geojson"));
    current.put(
        MimeTypeConfig.class,
        new MimeTypeConfigImpl(
            "nitf",
            "NITF Content Resolver",
            23,
            ".nitf",
            "image/nitf",
            ".ntf",
            "image/nitf",
            ".nsf",
            "image/nitf",
            ".nsif",
            "image/nitf",
            ".r0",
            "image/nitf",
            ".r1",
            "image/nitf",
            ".r2",
            "image/nitf",
            ".r3",
            "image/nitf",
            ".r4",
            "image/nitf",
            ".r5",
            "image/nitf",
            ".r6",
            "image/nitf",
            ".r7",
            "image/nitf"));
    current.put(
        MimeTypeConfig.class,
        new SchemaMimeTypeConfigImpl(
            "xml", "XML Content Resolver", 22, "urn:catalog:metacard", ".xml", "text/xml"));
    current.put(
        MimeTypeConfig.class,
        new MimeTypeConfigImpl("bob", "Bob Content Resolver", 27, ".bob", "application/bob"));
  }

  @Override
  public <T extends ConfigSingleton> Optional<T> get(Class<T> type) {
    LOGGER.error("##### ConfigServiceImpl::get(key)");
    return current.get(type).stream().filter(type::isInstance).map(type::cast).findFirst();
  }

  @Override
  public <T extends ConfigGroup> Optional<T> get(Class<T> type, String id) {
    LOGGER.error("##### ConfigServiceImpl::get(key, id)");
    return configs(type).filter(c -> c.getId().equals(id)).findFirst();
  }

  @Override
  public <T extends ConfigGroup> Stream<T> configs(Class<T> type) {
    LOGGER.error("##### ConfigServiceImpl::configs(key)");
    // the filter ensures that only the config objets of the corresponding type that are also
    // instances of the given class are returned
    return current.get(Config.getType(type)).stream().filter(type::isInstance).map(type::cast);
  }
}
