/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.snmp4j;

import org.snmp4j.Snmp;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;

public class TrackableDefaultUdpTransportMapping extends DefaultUdpTransportMapping {
    private final Snmp4JAgentConfig agentConfig;
    private final long timestamp;
    private final String stack;

    public TrackableDefaultUdpTransportMapping(Snmp4JAgentConfig agentConfig) throws IOException {
        // Capture the related agent configuration
        this.agentConfig = Objects.requireNonNull(agentConfig);
        // Capture the current system time
        timestamp = System.currentTimeMillis();
        // Capture the calling stack
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        new Throwable().printStackTrace(printWriter);
        stack = result.toString();
    }
}
