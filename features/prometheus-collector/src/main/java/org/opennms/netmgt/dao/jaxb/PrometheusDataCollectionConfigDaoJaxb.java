/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.xml.AbstractMergingJaxbConfigDao;
import org.opennms.netmgt.config.prometheus.Collection;
import org.opennms.netmgt.config.prometheus.PrometheusDatacollectionConfig;
import org.opennms.netmgt.dao.PrometheusDataCollectionConfigDao;

public class PrometheusDataCollectionConfigDaoJaxb extends AbstractMergingJaxbConfigDao<PrometheusDatacollectionConfig, PrometheusDatacollectionConfig> implements PrometheusDataCollectionConfigDao {

    public PrometheusDataCollectionConfigDaoJaxb() {
        super(PrometheusDatacollectionConfig.class, "Prometheus Data Collection Configuration",
                Paths.get("etc", "prometheus-datacollection-config.xml"),
                Paths.get("etc", "prometheus-datacollection.d"));
    }

    @Override
    public PrometheusDatacollectionConfig translateConfig(PrometheusDatacollectionConfig config) {
        return config;
    }

    @Override
    public PrometheusDatacollectionConfig mergeConfigs(PrometheusDatacollectionConfig source, PrometheusDatacollectionConfig target) {
        if (target == null) {
            target = new PrometheusDatacollectionConfig();
        }
        return target.merge(source);
    }

    @Override
    public PrometheusDatacollectionConfig getConfig() {
        return getObject();
    }

    @Override
    public Collection getCollectionByName(String name) {
        final Collection collection = getConfig().getCollection().stream()
            .filter(c -> Objects.equals(name, c.getName())).findFirst().orElse(null);

        if (collection == null) {
            return null;
        }

        // Create a shallow clone
        Collection collectionWithGroups = new Collection();
        collectionWithGroups.setName(collection.getName());
        collectionWithGroups.setRrd(collection.getRrd());
        collectionWithGroups.getGroupRef().addAll(collection.getGroupRef());
        
        // Resolve the group references and add them to the clone
        final Set<String> referencedGroupNames = new HashSet<>(collection.getGroupRef());
        collectionWithGroups.getGroup().addAll(getConfig().getGroup().stream()
                .filter(g -> referencedGroupNames.contains(g.getName()))
                .collect(Collectors.toList()));

        return collectionWithGroups;
    }

}
