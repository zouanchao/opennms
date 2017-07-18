/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.xml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AbstractMergingJaxbConfigDaoTest {

    private ServiceRegistry registry = DefaultServiceRegistry.INSTANCE;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void canExtendTheConfiguration() throws IOException {
        Path someConfigFile = tempFolder.newFile("some-config.xml").toPath();
        Files.write(someConfigFile, "<some-config></some-config>".getBytes());
        File includeFolder = tempFolder.newFolder("some-config.d");

        SomeDao someDao = new SomeDao(someConfigFile, includeFolder.toPath());
        SomeConfig someConfig = someDao.getObject();

        // Starts off empty
        assertEquals(Lists.newArrayList(), someConfig.getKeys());

        // Extend the config
        GenericConfigProvider ext = new GenericConfigProvider("<some-config>" +
                "<key>x</key>" +
                "</some-config>");
        Map<String, String> props = Maps.newHashMap();
        props.put("type", SomeConfig.class.getCanonicalName());
        registry.register(ext, props, ConfigProvider.class);

        // Expect a key now
        someConfig = someDao.getObject();
        assertEquals(Lists.newArrayList("x"), someConfig.getKeys());
    }


    @XmlRootElement(name = "some-config")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class SomeConfig {
        @XmlElement(name = "key")
        private List<String> keys = new ArrayList<>();

        public void setKeys(List<String> keys) {
            this.keys = keys;
        }

        public List<String> getKeys() {
            return keys;
        }
    }

    public static class SomeDao extends AbstractMergingJaxbConfigDao<SomeConfig, SomeConfig> {

        public SomeDao(Path rootFile, Path includeFolder) {
            super(SomeConfig.class, "some config", rootFile, includeFolder);
        }

        @Override
        public SomeConfig translateConfig(SomeConfig config) {
            return config;
        }

        @Override
        public SomeConfig mergeConfigs(SomeConfig source, SomeConfig target) {
            if (target == null) {
                target = new SomeConfig();
            }
            if (source != null) {
                target.getKeys().addAll(source.getKeys());
            }
            return target;
        }
    }
    
    public static class GenericConfigProvider implements ConfigProvider {
        private final String value;
        private final Date created;
 
        private GenericConfigProvider(String value) {
            this.value = value;
            this.created = new Date();
        }

        @Override
        public String getConfig() {
            return value;
        }

        @Override
        public Date getLastUpdate() {
            return created;
        }
    }

}
