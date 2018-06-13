package org.opennms.features.kafka.collection.persistence;

import org.opennms.features.kafka.collection.persistence.proto.CollectionSetProtos;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;

public class CollectionSetMapper {

	public CollectionSetProtos.CollectionSet buildCollectionSetProtos(CollectionSet collectionSet) {
		CollectionSetProtos.CollectionSet.Builder builder = CollectionSetProtos.CollectionSet.newBuilder()
				.setTimestamp(collectionSet.getCollectionTimestamp().getTime());
		
		collectionSet.visit(new CollectionSetVisitor() {

			@Override
			public void visitCollectionSet(CollectionSet set) {
				
			}

			@Override
			public void visitResource(CollectionResource resource) {
				
				//CollectionResource.RESOURCE_TYPE_NODE;
			}

			@Override
			public void visitGroup(AttributeGroup group) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void visitAttribute(CollectionAttribute attribute) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void completeAttribute(CollectionAttribute attribute) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void completeGroup(AttributeGroup group) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void completeResource(CollectionResource resource) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void completeCollectionSet(CollectionSet set) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		return builder.build();
	}
}
