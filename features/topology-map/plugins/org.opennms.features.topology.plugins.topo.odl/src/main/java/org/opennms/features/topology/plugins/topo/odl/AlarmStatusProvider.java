/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.odl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import com.google.common.collect.Maps;

/**
 * Status provider heavily inspired by org.opennms.features.topology.plugins.topo.graphml.GraphMLVertexStatusProvider.
 *
 * @author jwhite
 */
public class AlarmStatusProvider implements StatusProvider {

    protected static class VertexStatus extends DefaultStatus {
        public VertexStatus(OnmsSeverity severity, long count) {
            super(severity.getLabel(), count);
        }

        public VertexStatus(OnmsSeverity severity) {
            this(severity, 0);
        }

    }

    private final AlarmDao m_alarmDao;

    public AlarmStatusProvider(AlarmDao alarmDao) {
        m_alarmDao = Objects.requireNonNull(alarmDao);
    }

    @Override
    public String getNamespace() {
        return OpendaylightTopologyProvider.TOPOLOGY_NAMESPACE_ODL;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace() != null && getNamespace().equals(namespace);
    }

    @Override
    public Map<VertexRef, Status> getStatusForVertices(VertexProvider vertexProvider, Collection<VertexRef> vertices, Criteria[] criteria) {
        // All vertices for the current vertexProvider
        final List<Vertex> filteredVertices = vertices.stream()
                .filter(eachVertex -> contributesTo(eachVertex.getNamespace()) && eachVertex instanceof Vertex)
                .map(eachVertex -> (Vertex) eachVertex)
                .collect(Collectors.toList());

        // Find the set of node ids
        final Set<Integer> nodeIds = filteredVertices.stream()
                .map(v -> v.getNodeID())
                .collect(Collectors.toSet());
        nodeIds.remove(null);

        // Now grab the alarm summary for each node id
        final Map<Integer, AlarmSummary> nodeIdToAlarmSummaryMap = getAlarmSummaries(nodeIds);

        // Set the result
        final Map<VertexRef, Status> resultMap = Maps.newHashMap();
        for (Vertex eachVertex : filteredVertices) {
            AlarmSummary alarmSummary = nodeIdToAlarmSummaryMap.get(eachVertex.getNodeID());
            DefaultStatus status = alarmSummary == null ? new VertexStatus(OnmsSeverity.NORMAL) : new VertexStatus(alarmSummary.getMaxSeverity(), alarmSummary.getAlarmCount());
            resultMap.put(eachVertex, status);
        }
        return resultMap;
    }

    private Map<Integer , AlarmSummary> getAlarmSummaries(Set<Integer> nodeIds) {
        return m_alarmDao.getNodeAlarmSummariesIncludeAcknowledgedOnes(new ArrayList<>(nodeIds))
                    .stream()
                    .collect(Collectors.toMap(AlarmSummary::getNodeId, Function.identity())); 
    }
}
