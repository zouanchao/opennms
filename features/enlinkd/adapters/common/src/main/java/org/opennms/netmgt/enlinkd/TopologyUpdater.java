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

package org.opennms.netmgt.enlinkd;

import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyService;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyRef;
import org.opennms.netmgt.topologies.service.api.OnmsTopologySegment;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyUpdater;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public abstract class TopologyUpdater extends Discovery implements OnmsTopologyUpdater {

    public static OnmsTopologyProtocol create(ProtocolSupported protocol) throws OnmsTopologyException {
            return OnmsTopologyProtocol.create(protocol.name());
    }
    
    public static OnmsTopologyVertex create(NodeTopologyEntity node, IpInterfaceTopologyEntity primary) throws OnmsTopologyException {
        OnmsTopologyVertex vertex = OnmsTopologyVertex.create(node.getId().toString(), 
                                         node.getLabel(), 
                                         InetAddressUtils.str(primary.getIpAddress()), 
                                         node.getSysObjectId());
        vertex.setNodeid(node.getId());
        vertex.setToolTipText(Topology.getNodeTextString(node, primary));
        return vertex;
    }

    private static final Logger LOG = LoggerFactory.getLogger(TopologyUpdater.class);

    private OnmsTopologyDao m_topologyDao;
    private NodeTopologyService m_nodeTopologyService;
    private TopologyService m_topologyService;

    private OnmsTopology m_topology;
    private boolean m_runned = false;
    private boolean m_registered = false;

    public TopologyUpdater(
            TopologyService topologyService,
            OnmsTopologyDao topologyDao, NodeTopologyService nodeTopologyService) {
        super();
        m_topologyDao = topologyDao;
        m_topologyService = topologyService;
        m_nodeTopologyService = nodeTopologyService;
        m_topology = new OnmsTopology();
    }            
    
    public void register() {
        if (m_registered) {
            return;
        }
        try {
            m_topologyDao.register(this);
            m_registered  = true;
            LOG.info("register: protocol:{}", getProtocol());
        } catch (OnmsTopologyException e) {
            LOG.error("register: protocol:{} {}", e.getProtocol(),e.getMessage());
        }
    }
    
    public void unregister() {
        if (!m_registered) {
            return;
        }
        try {
            m_topologyDao.unregister(this);
            m_registered = false;
            LOG.info("unregister: protocol:{}", getProtocol());
        } catch (OnmsTopologyException e) {
            LOG.error("unregister: protocol:{} {}", e.getProtocol(),e.getMessage());
        }
    }
    private <T extends OnmsTopologyRef>void update(T topoObject) {
        try {
            m_topologyDao.update(this, OnmsTopologyMessage.update(topoObject,getProtocol()));
        } catch (OnmsTopologyException e) {
            LOG.error("update: status:{} id:{} protocol:{} message{}", e.getMessageStatus(), e.getId(), e.getProtocol(),e.getMessage());
        }
    }

    private <T extends OnmsTopologyRef>void delete(T topoObject) {
        try {
            m_topologyDao.update(this, OnmsTopologyMessage.delete(topoObject,getProtocol()));
        } catch (OnmsTopologyException e) {
            LOG.error("delete: status:{} id:{} protocol:{} message{}", e.getMessageStatus(), e.getId(), e.getProtocol(),e.getMessage());
        }
    }

    private <T extends OnmsTopologyRef>void create(T topoObject) {
        try {
            m_topologyDao.update(this, OnmsTopologyMessage.create(topoObject,getProtocol()));
        } catch (OnmsTopologyException e) {
            LOG.error("create: status:{} id:{} protocol:{} message{}", e.getMessageStatus(), e.getId(), e.getProtocol(),e.getMessage());
       }
    }

    @Override
    public void runDiscovery() {
        LOG.debug("run: start {}", getName());
        if (!m_runned) {
            synchronized (m_topology) {
                try {
                    m_topology = buildTopology();
                    m_runned = true;
                    m_topologyService.parseUpdates();
                    m_topology.getVertices().stream().forEach(v -> create(v));
                    m_topology.getEdges().stream().forEach(g -> create(g));
                    LOG.debug("run: {} first run topology calculated", getName());
                } catch (OnmsTopologyException e) {
                    LOG.error("run: {} first run: cannot build topology",getName(), e);
                    return;
                }
            }
        } else if (m_topologyService.parseUpdates()) {
            m_topologyService.refresh();
            LOG.debug("run: updates {}, recalculating topology ", getName());
            OnmsTopology topo;
            try {
                topo = buildTopology();
            } catch (OnmsTopologyException e) {
                LOG.error("cannot build topology", e);
                return;
            }
            synchronized (m_topology) {
                m_topology.getVertices().stream().filter(v -> !topo.hasVertex(v.getId())).forEach(v -> delete(v));
                m_topology.getEdges().stream().filter(g -> !topo.hasEdge(g.getId())).forEach(g -> delete(g));

                topo.getVertices().stream().filter(v -> !m_topology.hasVertex(v.getId())).forEach(v -> create(v));
                topo.getEdges().stream().filter(g -> !m_topology.hasEdge(g.getId())).forEach(g -> create(g));
                
                topo.getEdges().stream().filter(g -> m_topology.hasEdge(g.getId())).forEach(g -> {
                    OnmsTopologySegment og = m_topology.getEdge(g.getId()); 
                    boolean updated = false;
                    for (OnmsTopologyPort op: og.getSources()) {
                        if (!g.hasPort(op.getId())) {
                            update(g);
                            updated=true;
                            break;
                        }
                    }
                    if (!updated) {
                        for (OnmsTopologyPort p: g.getSources()) {
                            if (!og.hasPort(p.getId())) {
                                update(g);
                                break;
                            }
                        }
                    }
                });
                m_topology = topo;
            }
        }
        LOG.debug("run: end {}", getName());
    }

    public OnmsTopologyDao getTopologyDao() {
        return m_topologyDao;
    }

    public NodeTopologyService getNodeTopologyService() {
        return m_nodeTopologyService;
    }

    public Map<Integer, NodeTopologyEntity> getNodeMap() {
        return m_nodeTopologyService.findAllNode().stream().collect(Collectors.toMap(node -> node.getId(), node -> node, (n1,n2) ->n1));
    }
    
    public Map<Integer, IpInterfaceTopologyEntity> getIpPrimaryMap() {
        return m_nodeTopologyService.findAllIp().
                stream().
                collect
                (
                      Collectors.toMap
                      (
                           ip -> ip.getNodeId(), 
                           ip -> ip, 
                           (ip1,ip2) -> getPrimary(ip1, ip2)
                      )
                );
    }
    
    public Table<Integer, Integer,SnmpInterfaceTopologyEntity> getSnmpInterfaceTable() {
        Table<Integer, Integer,SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = HashBasedTable.create();
        for (SnmpInterfaceTopologyEntity snmp: m_nodeTopologyService.findAllSnmp()) {
            if (!nodeToOnmsSnmpTable.contains(snmp.getNodeId(),snmp.getIfIndex())) {
                nodeToOnmsSnmpTable.put(snmp.getNodeId(),snmp.getIfIndex(),snmp);
            }
        }
        return nodeToOnmsSnmpTable;
    }
    
    private static IpInterfaceTopologyEntity getPrimary(IpInterfaceTopologyEntity n1, IpInterfaceTopologyEntity n2) {
        if (PrimaryType.PRIMARY.equals(n2.getIsSnmpPrimary()) ) {
            return n2;
        }
        return n1;
    }
    public abstract OnmsTopology buildTopology() throws OnmsTopologyException;
    
    @Override
    public OnmsTopology getTopology() {
        synchronized (m_topology) {
            return m_topology.clone();
        }
    }

    public boolean isRegistered() {
        return m_registered;
    }

    public void setRegistered(boolean registered) {
        m_registered = registered;
    }

    public void setTopology(OnmsTopology topology) {
        m_topology = topology;
    }

    public boolean isRunned() {
        return m_runned;
    }

    public void setRunned(boolean runned) {
        m_runned = runned;
    }
                
}

