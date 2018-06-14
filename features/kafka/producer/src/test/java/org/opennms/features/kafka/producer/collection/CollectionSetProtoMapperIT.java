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

package org.opennms.features.kafka.producer.collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.kafka.producer.collection.CollectionSetMapper;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/applicationContext-kafka.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false, tempDbClass = MockDatabase.class, reuseDatabase = false)
public class CollectionSetProtoMapperIT {

	@Autowired
	private DatabasePopulator databasePopulator;
	
	@Autowired
	private CollectionSetMapper collectionSetMapper;

	@Test
	public void testCollecitonSetProto() throws UnknownHostException {

		databasePopulator.populateDatabase();
		OnmsNode node = databasePopulator.getNode2();
		CollectionAgent agent = new MockCollectionAgent(node.getId(), "test", InetAddress.getLocalHost());
		CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(1)).build();
		CollectionSetProtos.CollectionSet protos = collectionSetMapper.buildCollectionSetProtos(collectionSet);
		NodeLevelResource nodeResource = new NodeLevelResource(node.getId());

		collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
				.withNumericAttribute(nodeResource, "group1", "node2", 100, AttributeType.GAUGE).build();
		protos = collectionSetMapper.buildCollectionSetProtos(collectionSet);
		assertThat(protos.getResourceList(), hasSize(1));
		assertThat(protos.getTimestamp(), equalTo(2L));
		CollectionSetProtos.CollectionSetResource resource1 = protos.getResource(0);
		assertThat(resource1, notNullValue());
		assertThat(resource1.getNode(), notNullValue());
		assertThat(resource1.getNode().getNodeId() , equalTo(2L));
		assertThat(resource1.getNumericList(), hasSize(1));
		assertThat(resource1.getNumericList().get(0).getGroup() , equalTo("group1"));
		assertThat(resource1.getNumericList().get(0).getName(), equalTo("node2"));
		assertThat(resource1.getNumericList().get(0).getValue(), equalTo(100.0));
		assertThat(resource1.getStringList(), hasSize(0));
	}

}
