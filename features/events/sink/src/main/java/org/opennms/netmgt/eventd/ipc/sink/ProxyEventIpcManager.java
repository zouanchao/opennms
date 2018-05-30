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

import java.util.Collection;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

// TODO MVR what do to with addEventListener or removeEventListener, as this does not make sense for proxying events

/**
 * EventIpcManager which forwards all events to the sink module.
 */
public class ProxyEventIpcManager implements EventIpcManager {

    private final AsyncDispatcher<EventLogDTO> asyncDispatcher;
    private final SyncDispatcher<EventLogDTO> syncDispatcher;

    public ProxyEventIpcManager(MessageDispatcherFactory messageDispatcherFactory) {
        final EventSinkModule eventSinkModule = new EventSinkModule();
        asyncDispatcher = messageDispatcherFactory.createAsyncDispatcher(eventSinkModule);
        syncDispatcher = messageDispatcherFactory.createSyncDispatcher(eventSinkModule);
    }

    @Override
    public void sendNow(Event event) {
        Events events = new Events();
        events.addEvent(event);

        Log eventLog = new Log();
        eventLog.setEvents(events);

        sendNow(eventLog);
    }

    @Override
    public void sendNow(Log eventLog) {
        asyncDispatcher.send(new EventLogDTO(eventLog));
    }

    @Override
    public void sendNowSync(Event event) {
        Events events = new Events();
        events.addEvent(event);

        Log eventLog = new Log();
        eventLog.setEvents(events);

        sendNowSync(eventLog);
    }

    @Override
    public void sendNowSync(Log eventLog) {
        syncDispatcher.send(new EventLogDTO(eventLog));
    }

    @Override
    public void send(Event event) {
        sendNow(event);
    }

    @Override
    public void send(Log eventLog) {
        sendNow(eventLog);
    }

    @Override
    public void addEventListener(EventListener listener) {
        // TODO MVR not supported
    }

    @Override
    public void addEventListener(EventListener listener, Collection<String> ueis) {
        // TODO MVR not supported
    }

    @Override
    public void addEventListener(EventListener listener, String uei) {
        // TODO MVR not supported
    }

    @Override
    public void removeEventListener(EventListener listener) {
        // TODO MVR not supported
    }

    @Override
    public void removeEventListener(EventListener listener, Collection<String> ueis) {
        // TODO MVR not supported
    }

    @Override
    public void removeEventListener(EventListener listener, String uei) {
        // TODO MVR not supported
    }

    @Override
    public boolean hasEventListener(String uei) {
        // TODO MVR not supported
        return false;
    }
}
