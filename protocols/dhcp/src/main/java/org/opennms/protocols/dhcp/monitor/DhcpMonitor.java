/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.dhcp.monitor;

import java.io.IOException;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the DHCP service on remote interfaces as defined by RFC 2131.
 *
 * This class relies on the DHCP API provided by dhcp4java.
 *
 * The class implements the ServiceMonitor interface that allows it to be used
 * along with other plug-ins by the service poller framework.
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
@Distributable(DistributionContext.DAEMON)
public final class DhcpMonitor extends AbstractServiceMonitor {
	private static final Logger LOG = LoggerFactory.getLogger(DhcpMonitor.class);

    /**
     * Default retries.
     */
    public static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    public static final int DEFAULT_TIMEOUT = 3000;

    public static final String DEFAULT_MAC_ADDRESS = "00:06:0D:BE:9C:B2";

    /**
     * {@inheritDoc}
     *
     * Poll the specified address for DHCP service availability.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {

        // common parameters
        final int retry = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
        final int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);

        // DHCP-specific parameters
        final String macAddress = ParameterMap.getKeyedString(parameters, "macAddress", DEFAULT_MAC_ADDRESS);
        final boolean relayMode = ParameterMap.getKeyedBoolean(parameters, "relayMode", false);
        final boolean extendedMode = ParameterMap.getKeyedBoolean(parameters, "extendedMode", false);
        final String myAddress = ParameterMap.getKeyedString(parameters, "myIpAddress", "127.0.0.1");
        final String requestIpAddress = ParameterMap.getKeyedString(parameters, "requestIpAddress", "127.0.0.1");

        final TimeoutTracker tracker = new TimeoutTracker(parameters, retry, timeout);
        final Transaction transaction = new Transaction(svc.getIpAddr(), macAddress, relayMode, myAddress, extendedMode, requestIpAddress, timeout);
        for (tracker.reset(); tracker.shouldRetry() && !transaction.isSuccess(); tracker.nextAttempt()) {
            try {
                Dhcpd.addTransaction(transaction);
            } catch (IOException e) {
                e.printStackTrace();
                return PollStatus.unavailable("An unexpected exception occurred during DHCP polling");
            }
        }

        return transaction.isSuccess() ? PollStatus.available() : PollStatus.unavailable("DHCP service unavailable");
    }
    
}
