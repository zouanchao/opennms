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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.factory;

import java.util.Objects;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.ParserFactory;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Netflow5UdpParser;

public class Netflow5UdpParserFactory implements ParserFactory {

    private final TelemetryRegistry telemetryRegistry;

    public Netflow5UdpParserFactory(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = Objects.requireNonNull(telemetryRegistry);
    }

    @Override
    public Class<? extends org.opennms.netmgt.telemetry.api.receiver.Parser> getBeanClass() {
        return Netflow5UdpParser.class;
    }

    @Override
    public org.opennms.netmgt.telemetry.api.receiver.Parser createBean(final ParserDefinition parserDefinition) {
        final AsyncDispatcher<TelemetryMessage> dispatcher = telemetryRegistry.getDispatcher(parserDefinition.getQueueName());
        return new Netflow5UdpParser(parserDefinition.getName(), dispatcher);
    }
}
