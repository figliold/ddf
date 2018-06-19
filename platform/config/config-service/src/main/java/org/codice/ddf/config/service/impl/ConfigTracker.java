package org.codice.ddf.config.service.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.codice.ddf.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConfigTracker {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTracker.class);

  private Multimap<Class<?>, Config> previous = HashMultimap.create();

  private Multimap<Class<?>, Config> current = HashMultimap.create();

  public void updateCurrent(Set<Config> configs) {
    LOGGER.error("##### Start updateCurrent()");
    updatePrevious();
    // configs.forEach(this::updateCurrent);
    LOGGER.error("##### current before: {}; size: {}", current, current.size());
    LOGGER.error("##### size: {}", configs.size());
    for (Config o : configs) {
      updateCurrent(o);
    }
    // LOGGER.error("##### current after: {}; size: {}", current, current.size());
    LOGGER.error(
        dump(
            String.format(
                "The current configuration contains %s entries after update:", current.size()),
            current));
    LOGGER.error("##### End updateCurrent()");
  }

  public Multimap<Class<?>, Config> computeUpdates() {
    LOGGER.error("##### Start ConfigServiceImpl::computeUpdates");
    Multimap<Class<?>, Config> updates =
        Multimaps.filterEntries(current, e -> !previous.containsEntry(e.getKey(), e.getValue()));
    LOGGER.error("##### updates: {}", updates);
    LOGGER.error("##### End ConfigServiceImpl::computeUpdates");
    return updates;
  }

  public Multimap<Class<?>, Config> computeRemoved() {
    LOGGER.error("##### Start ConfigServiceImpl::computeRemoved");
    Multimap<Class<?>, Config> removed =
        Multimaps.filterEntries(previous, e -> !current.containsEntry(e.getKey(), e.getValue()));
    LOGGER.error("##### updates: {}", removed);
    LOGGER.error("##### End ConfigServiceImpl::computeRemoved");
    return removed;
  }

  private void updatePrevious() {
    LOGGER.error("##### Start ConfigServiceImpl::updatePrevious()");
    LOGGER.error("##### previous before: {}; size: {}", previous, previous.size());
    this.previous.putAll(this.current);
    LOGGER.error("##### previous after: {}; size: {}", previous, previous.size());
    LOGGER.error("##### End ConfigServiceImpl::updatePrevious()");
  }

  private void updateCurrent(Config o) {
    //    current.put(o.getClass(), (Config) o);
    current.put(o.getClass(), o);
  }

  private String dump(String prefix, Multimap<Class<?>, Config> configs) {
    StringBuilder builder = new StringBuilder();
    builder.append("\n" + prefix);
    for (Map.Entry<Class<?>, Collection<Config>> entry : configs.asMap().entrySet()) {
      dumpEntry(builder, entry.getKey(), entry.getValue());
    }
    return builder.toString();
  }

  private void dumpEntry(StringBuilder builder, Class<?> key, Collection<Config> configs) {
    builder.append("\n{\n   key: " + key);
    builder.append("\n   values:\n   [");
    configs.stream().forEach(c -> builder.append("\n      " + c));
    builder.append("\n   ]");
    builder.append("\n}");
  }

  //  private void dump(String prefix, Multimap<Class<?>, Config> configs) {
  //    // configs.asMap().forEach(this::log);
  //  }

  //    private void log(Class<?> key, Collection<Config> configs) {
  //        StringBuilder builder = new StringBuilder();
  //        builder.append("\n{\n   key: " + key);
  //        builder.append("\n   values:\n   [");
  //        configs.stream().forEach(c -> builder.append("\n      " + c));
  //        builder.append("\n   ]");
  //        builder.append("\n}\n");
  //        LOGGER.error(builder.toString());
  //    }
}
