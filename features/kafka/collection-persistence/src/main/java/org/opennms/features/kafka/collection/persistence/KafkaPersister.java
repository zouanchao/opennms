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

package org.opennms.features.kafka.collection.persistence;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.xml.XmlHandler;
import org.opennms.netmgt.collection.api.AbstractPersister;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;

public class KafkaPersister extends AbstractPersister {
    

    public static final String KAFKA_CONFIG_PID = "org.opennms.features.kafka.collection.persistence";
    public static final String KAFKA_CONFIG_SYS_PROP_PREFIX = KAFKA_CONFIG_PID + ".";
    private final Properties kafkaConfig = new Properties();
    private KafkaProducer<String,byte[]> producer;

    protected KafkaPersister(ServiceParameters params, RrdRepository repository) {
        super(params, repository);
    }

    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException {
       
    }
    
    /** {@inheritDoc} */
    @Override
    public void visitCollectionSet(CollectionSet set) {
        
        if (!(set instanceof CollectionSetDTO)) {
            return ;
        }

        // Defaults
        kafkaConfig.clear();
        kafkaConfig.put("key.serializer", StringSerializer.class.getCanonicalName());
        kafkaConfig.put("value.serializer", ByteArraySerializer.class.getCanonicalName());
        // Find all of the  system properties that start with 'org.opennms.features.kafka.collection.persistence'
        // and add them to the config. See https://kafka.apache.org/10/documentation.html#producerconfigs
        // for the list of supported properties
        for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
            final Object keyAsObject = entry.getKey();
            if (keyAsObject == null ||  !(keyAsObject instanceof String)) {
                continue;
            }
            final String key = (String)keyAsObject;

            if (key.length() > KAFKA_CONFIG_SYS_PROP_PREFIX.length()
                    && key.startsWith(KAFKA_CONFIG_SYS_PROP_PREFIX)) {
                final String kafkaConfigKey = key.substring(KAFKA_CONFIG_SYS_PROP_PREFIX.length());
                kafkaConfig.put(kafkaConfigKey, entry.getValue());
            }
        }
        
        XmlHandler<CollectionSetDTO> xmlHandler = new XmlHandler<CollectionSetDTO>(CollectionSetDTO.class);
        String collectionSet = xmlHandler.marshal((CollectionSetDTO)set);
        producer = new KafkaProducer<>(kafkaConfig);
        final ProducerRecord<String,byte[]> record = new ProducerRecord<>("collection.persist.xml", collectionSet.getBytes(StandardCharsets.UTF_8));
        Future<RecordMetadata> future = producer.send(record);
        // Wait till we actually persist record in kafka
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    

}
