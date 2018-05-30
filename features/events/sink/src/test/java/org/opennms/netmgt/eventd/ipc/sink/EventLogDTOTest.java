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

package org.opennms.netmgt.eventd.ipc.sink;

import static org.opennms.netmgt.events.api.EventConstants.PARM_NODE_LABEL;

import java.util.Date;

import org.junit.Test;
import org.opennms.core.xml.XmlHandler;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;

public class EventLogDTOTest {

    @Test
    public void verifyMarshalAndUnmarshal() {
        Event event = new Event();
        event.setUei(EventConstants.NODE_ADDED_EVENT_UEI);
        event.setTime(new Date());
        event.setSource("Karaf Shell");
        event.setNodeid(1337L);
        event.addParm(new Parm(PARM_NODE_LABEL, "dummy node"));

        Log log = new Log();
        Events events = new Events();
        events.addEvent(event);
        log.setEvents(events);

        final EventLogDTO eventLogDTO = new EventLogDTO(log);
        eventLogDTO.setDate(new Date());

        final XmlHandler<EventLogDTO> xmlHandler = new XmlHandler<>(EventLogDTO.class);
        final String xml = xmlHandler.marshal(eventLogDTO);
        final EventLogDTO read = xmlHandler.unmarshal(xml);

        System.out.println(read);
    }
}
