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

package org.opennms.core.spring;

import org.opennms.core.config.api.ConfigurationProvider;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ReloadableResource implements ConfigurationProvider {

    private final Resource resource;
    private long checksum;
    private long lastUpdated;

    public ReloadableResource(Resource resource) {
        this.resource = Objects.requireNonNull(resource);
        checkForUpdates();
    }

    public ReloadableResource(Resource resource, byte[] bytes) {
        this.resource = Objects.requireNonNull(resource);
        checkForUpdates(bytes);
    }

    @Override
    public byte[] getBytes() {
        try (InputStream is = resource.getInputStream()){
            return StreamUtils.copyToByteArray(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getLastUpdate() {
        return lastUpdated;
    }

    private void checkForUpdates() {
        checkForUpdates(getBytes());
    }

    private void checkForUpdates(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        long currentChecksum = crc32.getValue();
        if (currentChecksum != checksum) {
            checksum = currentChecksum;
            lastUpdated = System.currentTimeMillis();
        }
    }
}
