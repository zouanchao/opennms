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

package org.opennms.features.alarms.history.elastic;

import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.opennms.core.cache.Cache;
import org.opennms.features.alarms.history.elastic.dto.NodeDocumentDTO;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;

public class DocumentMapperTest {
    @Test
    public void test() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(1);
        alarm.setReductionKey("reductionKey");
        alarm.setFirstEventTime(new Date(System.currentTimeMillis()));
        alarm.setLastEventTime(new Date(System.currentTimeMillis() + 1));
        alarm.setCounter(2);
        alarm.setAlarmType(4);
        alarm.setLogMsg("logMsg");
        alarm.setDescription("descr");
        alarm.setOperInstruct("operInstr");
        alarm.setSeverityId(5);
        alarm.setSeverityLabel("severitylabel");
        alarm.archive();
        alarm.setManagedObjectType("motype");
        alarm.setManagedObjectInstance("moinstance");
        OnmsEvent lastEvent = new OnmsEvent();
        lastEvent.setId(1);
        lastEvent.setEventUei("eventuei");
        lastEvent.setEventLogMsg("event log msg");
        lastEvent.setEventDescr("event descr");
        lastEvent.setEventTime(new Date(System.currentTimeMillis() + 1));
        alarm.setLastEvent(lastEvent);
        alarm.setSituation(true);
        alarm.setAlarmAckUser("ackuser");
        alarm.setAlarmAckTime(new Date(System.currentTimeMillis() + 2));
        
        OnmsAlarm relatedAlarm = new OnmsAlarm();
        relatedAlarm.setId(2);
        relatedAlarm.setLastEvent(lastEvent);
        
        alarm.addRelatedAlarm(relatedAlarm);
        
        
//        OnmsMemo testMemo = new OnmsMemo();
//        testMemo.


        Cache<Integer, Optional<NodeDocumentDTO>> nodeCache = mock(Cache.class);
        DocumentMapper documentMapper = new DocumentMapper(nodeCache, System::currentTimeMillis);

        System.out.println(alarm);
        System.out.println(documentMapper.toDocument(alarm));
    }
}
