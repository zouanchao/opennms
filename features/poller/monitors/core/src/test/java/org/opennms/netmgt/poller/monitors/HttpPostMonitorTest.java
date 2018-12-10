/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.netmgt.utils.DnsUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
    "classpath:/META-INF/opennms/emptyContext.xml",
})
@JUnitConfigurationEnvironment
@DirtiesContext
public class HttpPostMonitorTest {
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testParameterSubstitution() throws UnknownHostException {
        HttpPostMonitor monitor = new HttpPostMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("url", "/{nodeLabel}.html");
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "HTTP");
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
        assertTrue(subbedParams.get("subbed-url").toString().equals("/localhost.html"));
    }
}
