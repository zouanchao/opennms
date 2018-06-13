package org.opennms.features.kafka.collection.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Test;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.features.kafka.collection.persistence.proto.CollectionSetProtos;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;

public class CollectionSetProtoTest {
    
    
    @Test
    public void testCollecitonSetProto() throws UnknownHostException {
        
        CollectionSetMapper mapper = new CollectionSetMapper();
        CollectionAgent agent = new MockCollectionAgent(1, "test", InetAddress.getLocalHost());
        CollectionSet collectionSet = new CollectionSetBuilder(agent)
        									.withTimestamp(new Date(1))
        									.build();
        CollectionSetProtos.CollectionSet protos = mapper.buildCollectionSetProtos(collectionSet);
        assertThat(protos.getResourceList(), hasSize(0));
        assertThat(protos.getTimestamp(), equalTo(1L));
        
        NodeLevelResource nodeResource = new NodeLevelResource(1);
        
        collectionSet = new CollectionSetBuilder(agent)
				.withTimestamp(new Date(2))
				.withNumericAttribute(nodeResource, "group", "node1", 100, AttributeType.GAUGE)
				.build();
        protos = mapper.buildCollectionSetProtos(collectionSet);
        assertThat(protos.getResourceList(), hasSize(1));
        assertThat(protos.getTimestamp(), equalTo(2L));
        CollectionSetProtos.CollectionSetResource resource1 = protos.getResource(0);
        assertThat(resource1, notNullValue());
        assertThat(resource1.getNode(), notNullValue());
        assertThat(resource1.getNumericList(), hasSize(1));
        assertThat(resource1.getStringList(), hasSize(0));
        //Todo validate numeric attribute, node resource
  
    }

}
