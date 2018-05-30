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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.xml.AbstractXmlSinkModule;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class EventSinkModule extends AbstractXmlSinkModule<EventLogDTO, EventLogDTO> {

    private static final String MODULE_ID = "Event";

    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_BATCH_INTERVAL_MS = 500;
    private static final int DEFAULT_QUEUE_SIZE = 10000;

    public EventSinkModule() {
        super(EventLogDTO.class);
    }

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public int getNumConsumerThreads() {
        return DEFAULT_NUM_THREADS;
    }

    @Override
    public AggregationPolicy<EventLogDTO, EventLogDTO, EventLogAccumulator> getAggregationPolicy() {
        return new AggregationPolicy<EventLogDTO, EventLogDTO, EventLogAccumulator>() {
            @Override
            public int getCompletionSize() {
                return DEFAULT_BATCH_SIZE;
            }

            @Override
            public int getCompletionIntervalMs() {
                return DEFAULT_BATCH_INTERVAL_MS;
            }

            @Override
            public Object key(EventLogDTO message) {
                final Calendar cal = Calendar.getInstance();
                cal.setTime(message.getDate());
                return cal.get(Calendar.MINUTE);
            }

            @Override
            public EventLogAccumulator aggregate(EventLogAccumulator accumulator, EventLogDTO newMessage) {
                if (accumulator == null) {
                    accumulator = new EventLogAccumulator();
                }
                accumulator.add(newMessage);
                return accumulator;
            }

            @Override
            public EventLogDTO build(EventLogAccumulator accumulator) {
                return accumulator.build();
            }
        };
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        return new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return DEFAULT_QUEUE_SIZE;
            }

            @Override
            public int getNumThreads() {
                return DEFAULT_NUM_THREADS;
            }

            @Override
            public boolean isBlockWhenFull() {
                return true;
            }
        };
    }

   public static class EventLogAccumulator {
        final List<EventLogDTO> arrayList = new ArrayList<>();

        public void add(EventLogDTO newMessage) {
            arrayList.add(newMessage);
        }

        public EventLogDTO build() {
            EventLogDTO eventLogDTO = new EventLogDTO();
            eventLogDTO.setDate(new Date());

            Log log = new Log();
            Events events = new Events();
            log.setEvents(events);

            // TODO MVR what do we do with the headers?!
            final List<Event> allEvents = new ArrayList<>();
            for (EventLogDTO eachLog : arrayList) {
                if (eachLog.getLog().getEvents() != null && eachLog.getLog().getEvents().getEventCollection() != null) {
                    allEvents.addAll(eachLog.getLog().getEvents().getEventCollection());
                }
            }
            Collections.sort(allEvents, Comparator.comparing(Event::getCreationTime));
            events.setEventCollection(allEvents);

            return new EventLogDTO(log);
        }
   }

}
