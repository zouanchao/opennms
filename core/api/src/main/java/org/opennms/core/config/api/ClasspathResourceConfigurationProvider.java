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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClasspathResourceConfigurationProvider implements ConfigurationProvider {

    private static final int READ_BUFFER_SIZE = 1024;
    private final byte[] bytes;
    private long lastUpdate = System.currentTimeMillis();

    public ClasspathResourceConfigurationProvider(String path) throws IOException {
        final ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(path)) {
            bytes = getBytesFrom(is);
        }
    }

    public ClasspathResourceConfigurationProvider(String path, BundleContext context) throws IOException {
        final Bundle bundle = context.getBundle();
        try(InputStream is = bundle.getEntry(path).openStream()) {
            bytes = getBytesFrom(is);
        }
    }

    private static byte[] getBytesFrom(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[READ_BUFFER_SIZE];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toByteArray();
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }
}
