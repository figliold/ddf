/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package ddf.test.itests.platform;

import static org.junit.Assert.fail;
import static com.jayway.restassured.RestAssured.given;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import com.jayway.restassured.response.Response;

import ddf.common.test.BeforeExam;
import ddf.test.itests.AbstractIntegrationTest;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestLoggingService extends AbstractIntegrationTest {

    private static final DynamicUrl LOGGING_SERVICE_JOLOKIA_URL = new DynamicUrl(DynamicUrl.SECURE_ROOT,
            HTTPS_PORT,
            "/jolokia/exec/org.codice.ddf.logging.platform.LoggingServiceBean:service=logging-service/retrieveLogEvents");

    @BeforeExam
    public void beforeTest() throws Exception {
        try {
            basePort = getBasePort();
            getServiceManager().waitForRequiredApps(getDefaultRequiredApps());
            getServiceManager().waitForAllBundles();
        } catch (Exception e) {
            LOGGER.error("Failed in @BeforeExam: ", e);
            fail("Failed in @BeforeExam: " + e.getMessage());
        }
    }

    @Test
    public void testLoggingServiceEndpoint() throws Exception {

        Response response = given().auth()
                .preemptive()
                .basic("admin", "admin")
                .expect()
                .statusCode(200)
                .when()
                .get(LOGGING_SERVICE_JOLOKIA_URL.getUrl());
    }
}
