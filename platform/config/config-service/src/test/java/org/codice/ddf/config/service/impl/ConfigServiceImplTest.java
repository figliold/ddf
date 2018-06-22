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
package org.codice.ddf.config.service.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codice.ddf.config.Config;
import org.codice.ddf.config.model.impl.CswFederationProfileConfigImpl;
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
    Multimap<String, Config> previous = HashMultimap.create();
    Multimap<String, Config> current = HashMultimap.create();

    CswFederationProfileConfigImpl s1 = new CswFederationProfileConfigImpl();
    s1.setId("csw1");
    s1.setName("csw1_name");
    s1.setUrl(new URL("https://localhost:8993/services/csw1"));

    CswFederationProfileConfigImpl s2 = new CswFederationProfileConfigImpl();
    s2.setId("csw2");
    s2.setName("csw2_name");
    s2.setUrl(new URL("https://localhost:8993/services/csw2"));

    current.put("csw1", s1);
    current.put("csw2", s2);

    System.out.println("previous: " + previous);
    System.out.println("current: " + current);

    Multimap<String, Config> updates =
        Multimaps.filterEntries(current, e -> !previous.containsEntry(e.getKey(), e.getValue()));

    System.out.println("\nupdates1: " + updates);

    CswFederationProfileConfigImpl s3 = new CswFederationProfileConfigImpl();
    s3.setId("csw2");
    s3.setName("csw2_new_name");
    s3.setUrl(new URL("https://localhost:8993/services/csw2"));

    previous.putAll(current);
    current.remove("csw2", s3);
    current.put("csw2", s3);
    System.out.println("\n\nprevious: " + previous);
    System.out.println("current: " + current);

    Multimap<String, Config> updates2 =
        Multimaps.filterEntries(current, e -> !previous.containsEntry(e.getKey(), e.getValue()));

    System.out.println("\nupdates2: " + updates2);
  }

  @Test
  public void test21() throws Exception {
    Multimap<Class<?>, Config> previous = HashMultimap.create();
    Multimap<Class<?>, Config> current = HashMultimap.create();

    CswFederationProfileConfigImpl s1 = new CswFederationProfileConfigImpl();
    s1.setId("csw1");
    s1.setName("csw1_name");
    s1.setUrl(new URL("https://localhost:8993/services/csw1"));

    CswFederationProfileConfigImpl s2 = new CswFederationProfileConfigImpl();
    s2.setId("csw2");
    s2.setName("csw2_name");
    s2.setUrl(new URL("https://localhost:8993/services/csw2"));

    current.put(CswFederationProfileConfigImpl.class, s1);
    current.put(CswFederationProfileConfigImpl.class, s2);

    System.out.println("previous: " + previous);
    System.out.println("current: " + current);

    Multimap<Class<?>, Config> updates =
        Multimaps.filterEntries(current, e -> !previous.containsEntry(e.getKey(), e.getValue()));

    System.out.println("\nupdates1: " + updates);

    CswFederationProfileConfigImpl s3 = new CswFederationProfileConfigImpl();
    s3.setId("csw2");
    s3.setName("csw2_new_name");
    s3.setUrl(new URL("https://localhost:8993/services/csw2"));

    previous.putAll(current);
    current.remove(CswFederationProfileConfigImpl.class, s3);
    current.put(CswFederationProfileConfigImpl.class, s3);
    System.out.println("\n\nprevious: " + previous);
    System.out.println("current: " + current);

    Multimap<Class<?>, Config> updates2 =
        Multimaps.filterEntries(current, e -> !previous.containsEntry(e.getKey(), e.getValue()));

    System.out.println("\nupdates2: " + updates2);
  }

  @Ignore
  @Test
  public void test24() throws Exception {
    Map<String, Config> previous = Maps.newHashMap();
    Map<String, Config> current = Maps.newHashMap();

    CswFederationProfileConfigImpl s1 = new CswFederationProfileConfigImpl();
    s1.setId("csw1");
    s1.setName("csw1_name");
    s1.setUrl(new URL("https://localhost:8993/services/csw1"));

    CswFederationProfileConfigImpl s2 = new CswFederationProfileConfigImpl();
    s2.setId("csw2");
    s2.setName("csw2_name");
    s2.setUrl(new URL("https://localhost:8993/services/csw2"));

    current.put("csw1", s1);
    current.put("csw2", s2);

    System.out.println("previous: " + previous);
    System.out.println("current: " + current);

    MapDifference<String, Config> diff = Maps.difference(previous, current);
    Map<String, Config> updates = diff.entriesOnlyOnRight();

    System.out.println("\nupdates1: " + updates);

    CswFederationProfileConfigImpl s3 = new CswFederationProfileConfigImpl();
    s3.setId("csw2");
    s3.setName("csw2_new_name");
    s3.setUrl(new URL("https://localhost:8993/services/csw2"));

    previous.putAll(current);
    // current.remove("csw2", s3);
    current.put("csw2", s3);
    System.out.println("\n\nprevious: " + previous);
    System.out.println("current: " + current);

    MapDifference<String, Config> diff2 = Maps.difference(previous, current);
    Set<Map.Entry<String, Config>> entries = diff2.entriesOnlyOnRight().entrySet();
    Set<Config> updates2 = entries.stream().map(Map.Entry::getValue).collect(Collectors.toSet());

    System.out.println("\nupdates2: " + updates2);
  }

  @Test
  public void test67() {
    Map<String, Integer> previous = ImmutableMap.of("csw1", 1, "csw2", 2, "csw3", 7);
    Map<String, Integer> current = ImmutableMap.of("csw1", 5, "csw2", 3, "csw3", 7);
    MapDifference<String, Integer> diff = Maps.difference(previous, current);
    Map<String, MapDifference.ValueDifference<Integer>> entriesDiffering = diff.entriesDiffering();
    System.out.println(entriesDiffering);
    Stream<Integer> updates = entriesDiffering.values().stream().map(e -> e.rightValue());
    System.out.println(updates.collect(Collectors.toSet()));
  }

  @Test
  public void test68() {
    Map<Class<?>, Number> previous =
        ImmutableMap.of(Integer.class, new Integer(1), Double.class, new Double(2.8));
    Map<Class<?>, Number> current =
        ImmutableMap.of(Integer.class, new Integer(2), Double.class, new Double(3.8));
    MapDifference<Class<?>, Number> diff = Maps.difference(previous, current);
    Map<Class<?>, MapDifference.ValueDifference<Number>> entriesDiffering = diff.entriesDiffering();
    System.out.println(entriesDiffering);
    Stream<Number> updates = entriesDiffering.values().stream().map(e -> e.rightValue());
    System.out.println(updates.collect(Collectors.toSet()));
  }

  //  @Test
  //  public void test84() throws Exception {
  //    ConfigTracker tracker = new ConfigTracker();
  //
  //    CswFederationProfileConfigImpl s1 = new CswFederationProfileConfigImpl();
  //    s1.setId("csw1");
  //    s1.setName("csw1_name");
  //    s1.setUrl(new URL("https://localhost:8993/services/csw1"));
  //
  //    CswFederationProfileConfigImpl s2 = new CswFederationProfileConfigImpl();
  //    s2.setId("csw2");
  //    s2.setName("csw2_name");
  //    s2.setUrl(new URL("https://localhost:8993/services/csw2"));
  //
  //    Set<Config> configGroup1 = new HashSet<>();
  //    configGroup1.add(s1);
  //    configGroup1.add(s2);
  //
  //    Set<Config> install = tracker.install(configGroup1).collect(Collectors.toSet());
  //
  //    System.out.println("\nupdates1: " + install);
  //
  //    CswFederationProfileConfigImpl s3 = new CswFederationProfileConfigImpl();
  //    s3.setId("csw2");
  //    s3.setName("csw2_new_name");
  //    s3.setUrl(new URL("https://localhost:8993/services/csw2"));
  //
  //    Set<Config> configGroup2 = new HashSet<>();
  //    configGroup2.add(s3);
  //
  //    Set<Config> updates = tracker.update(configGroup2).collect(Collectors.toSet());
  //
  //    //        MapDifference<String, Config> diff2 =
  //    //                Maps.difference(previous, current);
  //    //        Set<Map.Entry<String, Config>> entries = diff2.entriesOnlyOnRight().entrySet();
  //    //        Set<Config> updates2 =
  //    // entries.stream().map(Map.Entry::getValue).collect(Collectors.toSet());
  //
  //    System.out.println("\nupdates2: " + updates);
  //  }

  @Ignore
  @Test
  public void test3() throws Exception {
    //    Multimap<Class<?>, Config> previous = ArrayListMultimap.create();
    //    Multimap<Class<?>, Config> current = ArrayListMultimap.create();
    //
    //    Source s1 =
    //        new CswFederationProfile("csw1", 1, "csw1", new
    // URL("https://localhost:8993/services/csw"));
    //    Source s2 =
    //        new CswFederationProfile("csw1", 1, "csw1", new
    // URL("https://localhost:8993/services/csw"));
    //
    //    //        Source s3 = new CswFederationProfile("csw1", 1, "csw1", new
    //    // URL("https://localhost:8993/services/csw"));
    //    //        Source s4 = new CswFederationProfile("csw1", 1, "csw1", new
    //    // URL("https://localhost:8993/services/csw"));
    //
    //    previous.put(CswFederationProfile.class, s1);
    //
    //    current.put(CswFederationProfile.class, s1);
    //    current.put(CswFederationProfile.class, s2);
    //
    //    Multimap<Class<?>, Config> updates =
    //        Multimaps.filterEntries(current, e -> !previous.containsEntry(e.getKey(),
    // e.getValue()));
    //
    //    System.out.println(updates);
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
