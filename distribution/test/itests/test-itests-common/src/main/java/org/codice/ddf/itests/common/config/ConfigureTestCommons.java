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
package org.codice.ddf.itests.common.config;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.with;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import org.codice.ddf.configuration.DictionaryMap;
import org.codice.ddf.itests.common.AbstractIntegrationTest;
import org.codice.ddf.itests.common.AdminConfig;
import org.osgi.service.cm.Configuration;

public class ConfigureTestCommons {

  private static final String METACARD_VALIDATITY_FILTER_PLUGIN_SERVICE_PID =
      "ddf.catalog.metacard.validation.MetacardValidityFilterPlugin";

  private static final String METACARD_VALIDATITY_MARKER_PLUGIN_SERVICE_PID =
      "ddf.catalog.metacard.validation.MetacardValidityMarkerPlugin";

  private static final String METACARD_ATTRIBUTE_SECURITY_POLICY_PLUGIN_PID =
      "org.codice.ddf.catalog.security.policy.metacard.MetacardAttributeSecurityPolicyPlugin";

  private static final String AUTH_Z_REALM_PID = "ddf.security.pdp.realm.AuthzRealm";

  private static Dictionary<String, Object> configureService(
      String pid, Dictionary<String, Object> props, AdminConfig adminConfig) throws IOException {
    Configuration config = adminConfig.getConfiguration(pid, null);
    Dictionary<String, Object> oldProps = config.getProperties();
    config.update(props);

    // verify update
    with()
        .pollInterval(1, SECONDS)
        .await()
        .atMost(AbstractIntegrationTest.GENERIC_TIMEOUT_SECONDS, SECONDS)
        .ignoreExceptions()
        .until(
            () -> {
              Dictionary<String, Object> savedProps = config.getProperties();
              List<String> newKeys = Collections.list(props.keys());
              for (String key : newKeys) {
                if (!savedProps.get(key).equals(props.get(key))) {
                  return false;
                }
              }
              return true;
            });

    return oldProps;
  }

  public static Dictionary<String, Object> configureMetacardValidityFilterPlugin(
      List<String> securityAttributeMappings,
      boolean filterErrors,
      boolean filterWarnings,
      AdminConfig configAdmin)
      throws IOException {
    Dictionary<String, Object> properties = new DictionaryMap<>();
    properties.put("attributeMap", securityAttributeMappings);
    properties.put("filterErrors", String.valueOf(filterErrors));
    properties.put("filterWarnings", String.valueOf(filterWarnings));
    return configureMetacardValidityFilterPlugin(properties, configAdmin);
  }

  public static Dictionary<String, Object> configureMetacardValidityFilterPlugin(
      Dictionary<String, Object> props, AdminConfig configAdmin) throws IOException {
    return configureService(METACARD_VALIDATITY_FILTER_PLUGIN_SERVICE_PID, props, configAdmin);
  }

  public static Dictionary<String, Object> configureValidationMarkerPlugin(
      List<String> enforcedValidators,
      boolean enforceErrors,
      boolean enforceWarnings,
      AdminConfig configAdmin)
      throws IOException {
    Dictionary<String, Object> properties = new DictionaryMap<>();
    properties.put("enforcedMetacardValidators", enforcedValidators);
    properties.put("enforceErrors", String.valueOf(enforceErrors));
    properties.put("enforceWarnings", String.valueOf(enforceWarnings));
    return configureValidationMarkerPlugin(properties, configAdmin);
  }

  public static Dictionary<String, Object> configureValidationMarkerPlugin(
      Dictionary<String, Object> props, AdminConfig configAdmin) throws IOException {
    return configureService(METACARD_VALIDATITY_MARKER_PLUGIN_SERVICE_PID, props, configAdmin);
  }

  public static Dictionary<String, Object> configureMetacardAttributeSecurityFiltering(
      List<String> intersectAttributes, List<String> unionAttributes, AdminConfig configAdmin)
      throws IOException {
    Dictionary<String, Object> properties = new DictionaryMap<>();
    properties.put("intersectMetacardAttributes", intersectAttributes);
    properties.put("unionMetacardAttributes", unionAttributes);
    return configureMetacardAttributeSecurityFiltering(properties, configAdmin);
  }

  public static Dictionary<String, Object> configureMetacardAttributeSecurityFiltering(
      Dictionary<String, Object> properties, AdminConfig configAdmin) throws IOException {
    return configureService(METACARD_ATTRIBUTE_SECURITY_POLICY_PLUGIN_PID, properties, configAdmin);
  }

  public static Dictionary<String, Object> configureAuthZRealm(
      List<String> matchOneAttributes, AdminConfig configAdmin) throws IOException {
    Dictionary<String, Object> properties = new DictionaryMap<>();
    properties.put("matchOneMap", matchOneAttributes);
    return configureAuthZRealm(properties, configAdmin);
  }

  public static Dictionary<String, Object> configureAuthZRealm(
      Dictionary<String, Object> properties, AdminConfig configAdmin) throws IOException {
    return configureService(AUTH_Z_REALM_PID, properties, configAdmin);
  }
}
