/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.aws.sqs;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.common.aws.sqs.AmazonSQSConfig;
import org.opennms.core.ipc.common.aws.sqs.MapBasedSQSConfig;
import org.opennms.core.rpc.camel.MockMinionIdentity;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@Ignore("Requires access to AWS")
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-sqs.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-echo.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.core.ipc.aws.sqs.aws_use_http=true",
        "org.opennms.core.ipc.aws.sqs.aws_region=ca-central-1"})
public class EchoRpcBlueprintIT extends org.opennms.core.rpc.camel.EchoRpcBlueprintIT {

    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(MinionIdentity.class.getName(),
                new KeyValueHolder<Object, Dictionary>(new MockMinionIdentity(REMOTE_LOCATION_NAME),
                new Properties()));
        services.put(AmazonSQSConfig.class.getName(),
                new KeyValueHolder<Object, Dictionary>(new MapBasedSQSConfig(),
                        new Properties()));
    }

    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:OSGI-INF/blueprint/blueprint-rpc-server.xml";
    }

}
