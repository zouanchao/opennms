/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.config.api;

import com.google.common.collect.Maps;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ConfigReloadingContainerTest {

    private ServiceRegistry registry = DefaultServiceRegistry.INSTANCE;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static class SomeConfig {
        int sum;

        public SomeConfig() {

        }

        public SomeConfig(int sum) {
            this.sum = sum;
        }

        public int getSum() {
            return sum;
        }

        public void add(int x) {
            sum += x;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SomeConfig that = (SomeConfig) o;
            return sum == that.sum;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sum);
        }
    }

    @Test
    public void builderUsesSaneDefaults() {
        ConfigReloadContainer<SomeConfig> container = new ConfigReloadContainer.Builder<SomeConfig>()
                .build();
        assertThat(container.getObject(), is(nullValue()));
        assertThat(container.getLastUpdate(), is(equalTo(-1L)));
    }

    @Test
    public void canUseAnInitialObject() {
        Date before = new Date();
        SomeConfig config = new SomeConfig();
        ConfigReloadContainer<SomeConfig> container = new ConfigReloadContainer.Builder<SomeConfig>()
                .withInitialConfig(config)
                .build();
        assertThat(container.getObject(), is(equalTo(config)));
        assertThat(container.getLastUpdate(), is(greaterThanOrEqualTo(before.getTime())));
    }

    @Test
    public void canReloadOnGetWhenNoObjectIsGiven() throws IOException {
        final SomeConfig config = new SomeConfig();
        final AtomicInteger loadCount = new AtomicInteger();
        ConfigReloadContainer<SomeConfig> container = new ConfigReloadContainer.Builder<SomeConfig>()
                .withProvider(new StringConfigurationProvider("my-config-contents"))
                .withLoader(b -> {
                    loadCount.incrementAndGet();
                    return config;
                })
                .build();
        assertThat(container.getObject(), is(equalTo(config)));

        // Access the object another few times for good measure
        container.getObject();
        container.getObject();
        // Reload should only have been called once
        assertThat(loadCount.get(), is(equalTo(1)));
    }

    @Test
    public void canExtend() throws IOException {
        ConfigReloadContainer<SomeConfig> container = new ConfigReloadContainer.Builder<SomeConfig>()
                .withProvider(new StringConfigurationProvider("1"))
                .withLoader(b -> {
                    return new SomeConfig(b.length);
                })
                .withMerger((a,b) -> {
                    if (a == null) {
                        a = new SomeConfig();
                    }
                    a.add(b.getSum());
                    return a;
                })
                .withExtensionType("some-type")
                .build();

        // Verify the original object
        assertThat(container.getObject().getSum(), is(equalTo(1)));

        // Extend it
        StringConfigurationProvider ext = new StringConfigurationProvider("22");
        Map<String, String> props = Maps.newHashMap();
        props.put("type", "some-type");
        registry.register(ext, props, ConfigurationProvider.class);

        // Verify the extended object
        assertThat(container.getObject().getSum(), is(equalTo(3)));
    }
}
