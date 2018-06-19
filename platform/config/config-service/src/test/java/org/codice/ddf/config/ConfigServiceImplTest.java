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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.net.URL;
import org.junit.Ignore;
import org.junit.Test;

public class ConfigServiceImplTest {

  //  @Ignore
  //  @Test
  //  public void test1() throws Exception {
  //    ConfigServiceImpl c = new ConfigServiceImpl();
  //    Iterable<Object> cf = c.read(null);
  //    System.out.println(cf);
  //    for (Object o : cf) {
  //      System.out.println(o);
  //    }
  //
  //    c.install(null);
  //    c.update(null);
  //  }

  @Ignore
  @Test
  public void test2() throws Exception {
    Multimap<Class<?>, Config> previous = HashMultimap.create();
    Multimap<Class<?>, Config> current = HashMultimap.create();

    Source s1 =
        new CswFederationProfile("csw1", 1, "csw1", new URL("https://localhost:8993/services/csw"));
    Source s2 =
        new CswFederationProfile("csw1", 1, "csw1", new URL("https://localhost:8993/services/csw"));

    //        Source s3 = new CswFederationProfile("csw1", 1, "csw1", new
    // URL("https://localhost:8993/services/csw"));
    //        Source s4 = new CswFederationProfile("csw1", 1, "csw1", new
    // URL("https://localhost:8993/services/csw"));

    previous.put(CswFederationProfile.class, s1);

    current.put(CswFederationProfile.class, s1);
    current.put(CswFederationProfile.class, s2);

    Multimap<Class<?>, Config> updates =
        Multimaps.filterEntries(current, e -> !previous.containsEntry(e.getKey(), e.getValue()));

    System.out.println(updates);
  }

  @Ignore
  @Test
  public void test3() throws Exception {
    Multimap<Class<?>, Config> previous = ArrayListMultimap.create();
    Multimap<Class<?>, Config> current = ArrayListMultimap.create();

    Source s1 =
        new CswFederationProfile("csw1", 1, "csw1", new URL("https://localhost:8993/services/csw"));
    Source s2 =
        new CswFederationProfile("csw1", 1, "csw1", new URL("https://localhost:8993/services/csw"));

    //        Source s3 = new CswFederationProfile("csw1", 1, "csw1", new
    // URL("https://localhost:8993/services/csw"));
    //        Source s4 = new CswFederationProfile("csw1", 1, "csw1", new
    // URL("https://localhost:8993/services/csw"));

    previous.put(CswFederationProfile.class, s1);

    current.put(CswFederationProfile.class, s1);
    current.put(CswFederationProfile.class, s2);

    Multimap<Class<?>, Config> updates =
        Multimaps.filterEntries(current, e -> !previous.containsEntry(e.getKey(), e.getValue()));

    System.out.println(updates);
  }

  @Test
  public void test4() {
    //    Registry r = new Registry();
    //    Config c = (Config) r;
    //    System.out.println("done");
  }

  @Test
  public void test5() {
    //    ConfigServiceImpl cs = new ConfigServiceImpl();
    //    Set<Config> configs = cs.read(null);
  }

  @Test
  public void test6() throws Exception {
    //    ConfigServiceImpl c = new ConfigServiceImpl();
    //    Set<Config> cf = c.read(null);
    //    System.out.println(cf);
    //    for (Object o : cf) {
    //      System.out.println(o);
    //    }
    //
    //    c.install(null);
    //    Optional<CswFederationProfile> o = c.get(CswFederationProfile.class, "csw1");
    //    CswFederationProfile ci = o.get();
    //    System.out.println(ci.getUrl());
  }

  @Test
  public void test7() throws Exception {
    //    ConfigServiceImpl c = new ConfigServiceImpl();
    //    Set<Config> cf = c.read(null);
    //    System.out.println(cf);
    //    for (Object o : cf) {
    //      System.out.println("#: " + o);
    //    }
    //
    //    c.install(null);
    //    Optional<CswFederationProfile> o = c.get(CswFederationProfile.class, "csw14567");
    //    System.out.println(o.isPresent());
  }
}
