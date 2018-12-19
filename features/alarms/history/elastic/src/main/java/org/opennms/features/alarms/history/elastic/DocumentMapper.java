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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.opennms.core.cache.Cache;
import org.opennms.features.alarms.history.elastic.dto.AlarmDocumentDTO;
import org.opennms.features.alarms.history.elastic.dto.NodeDocumentDTO;
import org.opennms.features.alarms.history.elastic.mapping.AlarmMapper;
import org.opennms.netmgt.model.OnmsAlarm;

/**
 * Responsible for creating the DTO object from the corresponding
 * database entities.
 *
 * @author jwhite
 */
public class DocumentMapper {

    private final Cache<Integer, Optional<NodeDocumentDTO>> nodeInfoCache;
    private final Supplier<Long> currentTime;

    public DocumentMapper(Cache<Integer, Optional<NodeDocumentDTO>> nodeInfoCache, Supplier<Long> currentTime) {
        this.nodeInfoCache = Objects.requireNonNull(nodeInfoCache);
        this.currentTime = currentTime;
    }

    public AlarmDocumentDTO toDocument(OnmsAlarm alarm) {
        AlarmDocumentDTO doc = AlarmMapper.INSTANCE.onmsAlarmToAlarmDocumentDTO(alarm);
        doc.setUpdateTime(currentTime.get());
        
        return doc;
//
//        // Build and set the node document - cache these
//        if (alarm.getNodeId() != null) {
//            final Optional<NodeDocumentDTO> cachedNodeDoc = nodeInfoCache.getIfCached(alarm.getNodeId());
//            if (cachedNodeDoc != null && cachedNodeDoc.isPresent()) {
//                doc.setNode(cachedNodeDoc.get());
//            } else {
//                // We build the document here, rather than doing it in the call to the cache loader
//                // since we have complete access to the node in this context, and don't want to overload the
//                // cache key
//                final NodeDocumentDTO nodeDoc = toNode(alarm.getNode());
//                nodeInfoCache.put(alarm.getNodeId(), Optional.of(nodeDoc));
//                doc.setNode(nodeDoc);
//            }
//        }
//        // Related alarms
//        List<Integer> relatedAlarmIds = new LinkedList<>();
//        List<String> relatedAlarmReductionKeys = new LinkedList<>();
//        for (OnmsAlarm relatedAlarm : alarm.getRelatedAlarms()) {
//            relatedAlarmIds.add(relatedAlarm.getId());
//            relatedAlarmReductionKeys.add(relatedAlarm.getReductionKey());
//            doc.addRelatedAlarm(toRelatedAlarm(relatedAlarm));
//        }
//        doc.setRelatedAlarmIds(relatedAlarmIds);
//        doc.setRelatedAlarmReductionKeys(relatedAlarmReductionKeys);
//
//        // TODO: Set related alarm count
//
//        return doc;
    }

    public AlarmDocumentDTO createAlarmDocumentForDelete(int alarmId, String reductionKey) {
        final AlarmDocumentDTO doc = new AlarmDocumentDTO();
        doc.setId(alarmId);
        doc.setReductionKey(reductionKey);
        doc.setUpdateTime(currentTime.get());
        doc.setDeletedTime(currentTime.get());
        return doc;
    }
}
