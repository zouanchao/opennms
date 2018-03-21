/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest.rules;

import static io.restassured.RestAssured.preemptive;
import static org.opennms.smoketest.OpenNMSSeleniumTestCase.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.OpenNMSSeleniumTestCase.BASIC_AUTH_USERNAME;

import org.junit.rules.ExternalResource;
import org.opennms.smoketest.SmokeTestEnvironment;

import io.restassured.RestAssured;

public class RestTest extends ExternalResource {

    private SmokeTestEnvironment testEnvironment = SmokeTestEnvironment.DEFAULT;

    private final String basePath;

    public RestTest(String basePath) {
        this.basePath = basePath;
    }

    public RestTest() {
        this(null);
    }

//    @Override
//    public Statement apply(Statement base, Description description) {
//        return new Statement() {
//            @Override
//            public void evaluate() throws Throwable {
//                boolean didFail = true;
//                Throwable failure = null;
//                try {
//                    testEnvironment.before();
//                    RestTest.this.before();
//
//                    base.evaluate();
//                    didFail = false;
//                } catch (Throwable t) {
//                    failure = t;
//                    throw t;
//                } finally {
//                    RestTest.this.after();
//                    testEnvironment.after(didFail, failure);
//                }
//            }
//        };
//    }

    @Override
    protected void before() {
        RestAssured.baseURI = testEnvironment.getBaseUrl();
        RestAssured.port = testEnvironment.getServerHttpPort();
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
        if (basePath != null) {
            RestAssured.basePath = basePath;
        }
    }

    @Override
    protected void after() {
        RestAssured.reset();
    }
}
