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
package ddf.test.itests.catalog;

import static com.jayway.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.with;
import static org.codice.ddf.itests.common.catalog.CatalogTestCommons.ingest;
import static org.codice.ddf.itests.common.catalog.CatalogTestCommons.ingestXmlFromResourceAndWait;
import static org.codice.ddf.itests.common.catalog.CatalogTestCommons.query;
import static org.codice.ddf.itests.common.catalog.CatalogTestCommons.update;
import static org.codice.ddf.itests.common.config.ConfigureTestCommons.configureMetacardValidityFilterPlugin;
import static org.codice.ddf.itests.common.config.ConfigureTestCommons.configureValidationMarkerPlugin;
import static org.codice.ddf.itests.common.csw.CswQueryBuilder.PROPERTY_IS_LIKE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.ValidatableResponse;
import ddf.catalog.data.types.Validation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.codice.ddf.itests.common.AbstractIntegrationTest;
import org.codice.ddf.itests.common.catalog.CatalogTestCommons;
import org.codice.ddf.itests.common.csw.CswQueryBuilder;
import org.codice.ddf.test.common.LoggingUtils;
import org.codice.ddf.test.common.annotations.BeforeExam;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

/**
 * Tests catalog validation This test was created to pull out 16 tests in TestCatalog that were
 * starting/stopping the sample-validator each time. Since there is almost no overhead now for a new
 * class it is faster to just start the feature once for all 16 of the tests instead of toggling it
 * for each one.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class TestCatalogValidation extends AbstractIntegrationTest {

  @Rule public TestName testName = new TestName();

  public static String getGetRecordByIdProductRetrievalUrl() {
    return "?service=CSW&version=2.0.2&request=GetRecordById&NAMESPACE=xmlns="
        + "http://www.opengis.net/cat/csw/2.0.2&"
        + "outputFormat=application/octet-stream&outputSchema="
        + "http://www.iana.org/assignments/media-types/application/octet-stream&"
        + "id=placeholder_id";
  }

  public static String getSimpleXml(String uri) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
        + getFileContent(
            XML_RECORD_RESOURCE_PATH + "/SimpleXmlNoDecMetacard", ImmutableMap.of("uri", uri));
  }

  @BeforeExam
  public void beforeExam() {
    try {
      waitForSystemReady();
      getServiceManager().startFeature(true, "sample-validator");

      // start test with validation errors/warnings allowed in catalog/search results
      configureValidationMarkerPlugin(
          Collections.singletonList(""), false, false, getAdminConfig());
      configureMetacardValidityFilterPlugin(
          Collections.singletonList("invalid-state=guest"), false, false, getAdminConfig());
    } catch (Exception e) {
      LoggingUtils.failWithThrowableStacktrace(e, "Failed in @BeforeExam: ");
    }
  }

  @Before
  public void setup() {
    clearCatalogAndWait();
  }

  /* ***************** TEST ENFORCE VALIDATION ***************** */
  @Test
  public void testEnforceValidityErrorsOnly() throws Exception {
    // Configure marker plugin to enforce validator errors but not warnings
    Dictionary<String, Object> markerPluginProps =
        configureValidationMarkerPlugin(
            Collections.singletonList("sample-validator"), true, false, getAdminConfig());

    String warningId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String cleanId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    ingestXmlFromResourceWaitForFailure(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    // verify the clean and warning metacard can be queried
    query(cleanId, "xml", HttpStatus.SC_OK);
    query(warningId, "xml", HttpStatus.SC_OK);

    // Test updating
    String warningData = getFileContent(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String errorData = getFileContent(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");
    update(cleanId, warningData, MediaType.APPLICATION_XML, HttpStatus.SC_OK);
    update(cleanId, errorData, MediaType.APPLICATION_XML, HttpStatus.SC_BAD_REQUEST);

    // Reset marker plugin
    configureValidationMarkerPlugin(markerPluginProps, getAdminConfig());
  }

  @Test
  public void testEnforceValidityWarningsOnly() throws Exception {
    // Configure marker plugin to enforce warnings but not errors
    Dictionary<String, Object> markerPluginProps =
        configureValidationMarkerPlugin(
            Collections.singletonList("sample-validator"), false, true, getAdminConfig());

    ingestXmlFromResourceWaitForFailure(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String cleanId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    String errorId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    // verify the clean and error metacard can be queried
    query(cleanId, "xml", HttpStatus.SC_OK);
    query(errorId, "xml", HttpStatus.SC_OK);

    // Test updating
    String warningData = getFileContent(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String errorData = getFileContent(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");
    update(cleanId, warningData, MediaType.APPLICATION_XML, HttpStatus.SC_BAD_REQUEST);
    update(cleanId, errorData, MediaType.APPLICATION_XML, HttpStatus.SC_OK);

    // Reset marker plugin
    configureValidationMarkerPlugin(markerPluginProps, getAdminConfig());
  }

  @Test
  public void testEnforceValidityErrorsAndWarnings() throws Exception {
    // Configure marker plugin to enforce errors and warnings
    Dictionary<String, Object> markerPluginProps =
        configureValidationMarkerPlugin(
            Collections.singletonList("sample-validator"), true, true, getAdminConfig());

    ingestXmlFromResourceWaitForFailure(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String cleanId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    ingestXmlFromResourceWaitForFailure(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    // verify the clean metacard can be queried
    query(cleanId, "xml", HttpStatus.SC_OK);

    // Test updating with invalid data
    String warningData = getFileContent(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String errorData = getFileContent(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");
    update(cleanId, warningData, MediaType.APPLICATION_XML, HttpStatus.SC_BAD_REQUEST);
    update(cleanId, errorData, MediaType.APPLICATION_XML, HttpStatus.SC_BAD_REQUEST);

    // Reset marker plugin
    configureValidationMarkerPlugin(markerPluginProps, getAdminConfig());
  }

  @Test
  public void testNoEnforceValidityErrorsOrWarnings() throws Exception {
    // Configure marker plugin to enforce neither errors nor warnings
    Dictionary<String, Object> markerPluginProps =
        configureValidationMarkerPlugin(
            Collections.singletonList("sample-validator"), false, false, getAdminConfig());

    String warningId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String cleanId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    String errorId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    // verify the clean and warning metacard can be queried
    query(cleanId, "xml", HttpStatus.SC_OK);
    query(warningId, "xml", HttpStatus.SC_OK);
    query(errorId, "xml", HttpStatus.SC_OK);

    // Test updating
    String warningData = getFileContent(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String errorData = getFileContent(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");
    update(cleanId, warningData, MediaType.APPLICATION_XML, HttpStatus.SC_OK);
    update(cleanId, errorData, MediaType.APPLICATION_XML, HttpStatus.SC_OK);

    // Reset marker plugin
    configureValidationMarkerPlugin(markerPluginProps, getAdminConfig());
  }

  /* ***************** TEST VALIDATION QUERY ***************** */
  @Test
  public void testQueryByErrorFailedValidators() throws Exception {
    ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    String query =
        new CswQueryBuilder()
            .addAttributeFilter(
                PROPERTY_IS_LIKE, Validation.FAILED_VALIDATORS_ERRORS, "sample-validator")
            .getQuery();
    ValidatableResponse response =
        given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
            .body(query)
            .post(CSW_PATH.getUrl())
            .then();

    response.body(not(containsString("warning metacard")));
    response.body(not(containsString("clean metacard")));
    response.body(containsString("error metacard"));
  }

  @Test
  public void testQueryByWarningFailedValidators() throws Exception {
    ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    String query =
        new CswQueryBuilder()
            .addAttributeFilter(
                PROPERTY_IS_LIKE, Validation.FAILED_VALIDATORS_WARNINGS, "sample-validator")
            .getQuery();
    ValidatableResponse response =
        given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
            .body(query)
            .post(CSW_PATH.getUrl())
            .then();

    // clean metacard and warning metacard should be in results but not error one
    response.body(not(containsString("error metacard")));
    response.body(not(containsString("clean metacard")));
    response.body(containsString("warning metacard"));
  }

  /* ***************** TEST POST-QUERY VALIDITY FILTERING ***************** */
  @Test
  public void testFilterPluginWarningsOnly() throws Exception {
    String warningId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String cleanId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    String errorId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    // Configure to filter metacards with validation warnings but not validation errors
    Dictionary<String, Object> filterPluginProps =
        configureMetacardValidityFilterPlugin(
            Collections.singletonList("invalid-state=data-manager"), false, true, getAdminConfig());

    testWithRetry(
        () -> {
          query(warningId, "xml", HttpStatus.SC_NOT_FOUND);
          query(cleanId, "xml", HttpStatus.SC_OK);
          query(errorId, "xml", HttpStatus.SC_OK);
        });

    // Reset plugin
    configureMetacardValidityFilterPlugin(filterPluginProps, getAdminConfig());
  }

  @Test
  public void testFilterPluginErrorsOnly() throws Exception {
    String warningId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String cleanId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    String errorId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    // Configure to filter metacards with validation errors but not validation warnings
    Dictionary<String, Object> filterPluginProps =
        configureMetacardValidityFilterPlugin(
            Collections.singletonList("invalid-state=data-manager"), true, false, getAdminConfig());

    testWithRetry(
        () -> {
          query(warningId, "xml", HttpStatus.SC_OK);
          query(cleanId, "xml", HttpStatus.SC_OK);
          query(errorId, "xml", HttpStatus.SC_NOT_FOUND);
        });

    // Reset plugin
    configureMetacardValidityFilterPlugin(filterPluginProps, getAdminConfig());
  }

  @Test
  public void testFilterPluginWarningsAndErrors() throws Exception {
    String warningId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String cleanId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    String errorId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    // Configure to filter metacards with validation errors and validation warnings
    Dictionary<String, Object> filterPluginProps =
        configureMetacardValidityFilterPlugin(
            Collections.singletonList("invalid-state=data-manager"), true, true, getAdminConfig());

    testWithRetry(
        () -> {
          query(warningId, "xml", HttpStatus.SC_NOT_FOUND);
          query(cleanId, "xml", HttpStatus.SC_OK);
          query(errorId, "xml", HttpStatus.SC_NOT_FOUND);
        });

    // Reset plugin
    configureMetacardValidityFilterPlugin(filterPluginProps, getAdminConfig());
  }

  @Test
  public void testFilterPluginNoFiltering() throws Exception {
    String warningId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleWarningMetacard.xml");
    String cleanId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleCleanMetacard.xml");
    String errorId =
        ingestXmlFromResourceAndWait(XML_RECORD_RESOURCE_PATH + "/sampleErrorMetacard.xml");

    // Configure to not filter metacards with validation errors nor validation warnings
    Dictionary<String, Object> filterPluginProps =
        configureMetacardValidityFilterPlugin(
            Collections.singletonList("invalid-state=data-manager"),
            false,
            false,
            getAdminConfig());

    testWithRetry(
        () -> {
          query(warningId, "xml", HttpStatus.SC_OK);
          query(cleanId, "xml", HttpStatus.SC_OK);
          query(errorId, "xml", HttpStatus.SC_OK);
        });

    // Reset plugin
    configureMetacardValidityFilterPlugin(filterPluginProps, getAdminConfig());
  }

  /**
   * This method tries to ingest the given resource until it fails. This is needed because of the
   * async nature of setting configurations that would restrict/reject an ingest request.
   */
  private void ingestXmlFromResourceWaitForFailure(String resourceName) {
    String resourceString = getFileContent(resourceName);
    List<String> ids = new ArrayList<>();
    with()
        .pollInterval(1, SECONDS)
        .await()
        .atMost(30, SECONDS)
        .ignoreExceptions()
        .until(
            () -> {
              try {
                ids.add(ingest(resourceString, "text/xml", true));
              } catch (AssertionError ae) {
                return true;
              }
              return false;
            });
    ids.forEach(CatalogTestCommons::deleteMetacardAndWait);
  }

  /**
   * Setting configurations is performed asynchronously and there is no way to check if the
   * configured bean has received a configuration update. This method provides a best effort
   * workaround by retrying the test/assertions with a slight delay in between tries in an attempt
   * to let the configuration thread catch up. The Runnable.run() method will be called in each
   * attempt and all exceptions including AssertionErrors will be treated as a failed run and
   * retried.
   */
  private void testWithRetry(Runnable runnable) {

    with()
        .pollInterval(1, SECONDS)
        .await()
        .atMost(30, SECONDS)
        .ignoreExceptions()
        .until(
            () -> {
              runnable.run();
              return true;
            });
  }
}
