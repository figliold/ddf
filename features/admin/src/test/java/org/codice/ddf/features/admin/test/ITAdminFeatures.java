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
package org.codice.ddf.features.admin.test;

import static com.jayway.restassured.RestAssured.given;
import static org.codice.ddf.test.common.options.DebugOptions.defaultDebuggingOptions;
import static org.codice.ddf.test.common.options.DistributionOptions.kernelDistributionOption;
import static org.codice.ddf.test.common.options.FeatureOptions.addBootFeature;
import static org.codice.ddf.test.common.options.FeatureOptions.addFeatureRepo;
import static org.codice.ddf.test.common.options.LoggingOptions.defaultLogging;
import static org.codice.ddf.test.common.options.PortOptions.defaultPortsOptions;
import static org.codice.ddf.test.common.options.TestResourcesOptions.getTestResource;
import static org.codice.ddf.test.common.options.TestResourcesOptions.includeTestResources;
import static org.codice.ddf.test.common.options.VmOptions.defaultVmOptions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.ops4j.pax.exam.CoreOptions.options;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.codice.ddf.platform.logging.LogEvent;
import org.codice.ddf.sync.installer.api.SynchronizedInstaller;
import org.codice.ddf.test.common.LoggingUtils;
import org.codice.ddf.test.common.annotations.BeforeExam;
import org.codice.ddf.test.common.annotations.PaxExamRule;
import org.codice.ddf.test.common.features.FeatureUtilities;
import org.codice.ddf.test.common.features.TestUtilitiesFeatures;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;

/**
 * This is PROTOTYPE itest code which takes the tests from TestPlatform and deploys them in a
 * smaller container to speed test execution time up.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ITAdminFeatures {

  @Rule public PaxExamRule paxExamRule = new PaxExamRule(this);

  private static final String FEATURE_REPO_PATH = getTestResource("/features.xml");

  // TODO: Remove hardcoded port.
  private static final String LOGGING_SERVICE_JOLOKIA_URL =
      "https://localhost:20002/admin/jolokia/exec/org.codice.ddf.platform.logging.LoggingService:service=logging-service/retrieveLogEvents";

  // TODO: Remove hardcoded port.
  private static final String REPORT_GENERATION_URL =
      "https://localhost:20002/services/internal/metrics/report.xls";

  public static final String ADMIN = "admin";

  /**
   * Was getting the following error without the @ProbeBuilder. java.lang.LinkageError: loader
   * constraint violation: loader (instance of org/eclipse/osgi/internal/loader/EquinoxClassLoader)
   * previously initiated loading for a different type with name
   * "com/jayway/restassured/specification/RequestSpecification
   */
  @ProbeBuilder
  public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
    probe.setHeader(
        Constants.IMPORT_PACKAGE,
        "*,com.jayway.restassured.specification,com.jayway.restassured.response");
    return probe;
  }

  @Configuration
  public static Option[] examConfiguration() {
    return options(
        kernelDistributionOption(),
        defaultVmOptions(),
        defaultDebuggingOptions(),
        defaultPortsOptions(),
        defaultLogging(),
        includeTestResources(),
        addFeatureRepo(FeatureUtilities.toFeatureRepo(FEATURE_REPO_PATH)),
        addBootFeature(TestUtilitiesFeatures.testCommon(), TestUtilitiesFeatures.restAssured()));
  }

  @Inject private SynchronizedInstaller syncInstaller;

  @BeforeExam
  public void beforeTest() {
    System.out.println("\n##### Start BeforeExam #####");
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    try {
      syncInstaller.installFeatures(
          "platform-paxweb-jettyconfig",
          "web-container",
          "security-core",
          "security-policy-context",
          "security-web-sso-defaults",
          "security-guest",
          "security-sts-certificateclaimshandler",
          "security-idp",
          "security-certificate",
          "security-rest-authentication",
          "security-command-listener",
          "platform-api");
      syncInstaller.installFeatures("platform-logging", "admin-core-logviewer");
      syncInstaller.installFeatures("metrics-reporting");
      syncInstaller.waitForBootFinish();
    } catch (Exception e) {
      LoggingUtils.failWithThrowableStacktrace(e, "Failed in @BeforeExam: ");
    }
    System.out.println("##### End BeforeExam #####\n");
  }

  @Test
  public void testLoggingServiceEndpoint() {
    System.out.println("\n##### Start testLoggingServiceEndpoint #####");
    outputBundleHeaders(getProbeBundle());
    final Response response =
        given()
            .auth()
            .preemptive()
            .basic(ADMIN, ADMIN)
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Origin", LOGGING_SERVICE_JOLOKIA_URL)
            .expect()
            .statusCode(200)
            .when()
            .get(LOGGING_SERVICE_JOLOKIA_URL);

    final String bodyString = checkResponseBody(response, LOGGING_SERVICE_JOLOKIA_URL);

    final List events = JsonPath.given(bodyString).get("value");
    final Map firstEvent = (Map) events.get(0);
    final String levelOfFirstEvent = firstEvent.get("level").toString();
    final String unknownLevel = LogEvent.Level.UNKNOWN.getLevel();
    assertThat(
        String.format(
            "The level of an event returned by %s should not be %s",
            LOGGING_SERVICE_JOLOKIA_URL, unknownLevel),
        levelOfFirstEvent,
        not(equalTo(unknownLevel)));
    System.out.println("##### End testLoggingServiceEndpoint #####\n");
  }

  @Test
  public void testPlatformMetricsReportGeneration() {
    System.out.println("\n##### Start testPlatformMetricsReportGeneration #####");
    outputBundleHeaders(getProbeBundle());
    final Response response =
        given()
            .auth()
            .preemptive()
            .basic(ADMIN, ADMIN)
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Origin", REPORT_GENERATION_URL)
            .expect()
            .statusCode(200)
            .when()
            .get(REPORT_GENERATION_URL);

    checkResponseBody(response, REPORT_GENERATION_URL);
    System.out.println("##### End testPlatformMetricsReportGeneration #####\n");
  }

  private String checkResponseBody(final Response response, final String url) {
    final String bodyString = response.getBody().asString();
    assertThat(
        String.format("The response body from %s should not be empty", url),
        bodyString,
        not(isEmptyString()));
    return bodyString;
  }

  private Bundle getProbeBundle() {
    for (final Bundle bundle :
        FrameworkUtil.getBundle(this.getClass()).getBundleContext().getBundles()) {
      if (bundle.getSymbolicName().startsWith("PAXEXAM-PROBE-")) {
        return bundle;
      }
    }
    return null;
  }

  private void outputBundleHeaders(final Bundle bundle) {
    if (bundle != null) {
      final Dictionary<String, String> headers = bundle.getHeaders();
      final Enumeration<String> headerKeys = headers.keys();
      System.out.println("\n##### Start Bundle Headers #####");
      while (headerKeys.hasMoreElements()) {
        final String headerKey = headerKeys.nextElement();
        System.out.println(headerKey + ": " + headers.get(headerKey));
      }
      System.out.println("##### End Bundle Headers #####\n");
    }
  }
}
