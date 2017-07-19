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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class ReloadableFile implements ConfigurationProvider {

    private final File file;
    private long lastModified;
    private long lastFileSize;
    private long lastUpdated;

    public ReloadableFile(File file) {
        this.file = Objects.requireNonNull(file);
        checkForUpdates();
    }

    @Override
    public byte[] getBytes() {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getLastUpdate() {
        checkForUpdates();
        return lastUpdated;
    }

    private void checkForUpdates() {
        boolean didChange = false;
        if (file.lastModified() > lastModified) {
            lastModified = file.lastModified();
            didChange = true;
        }
        if (file.length() >= lastFileSize) {
            lastFileSize = file.length();
            didChange = true;
        }

        if (didChange) {
            lastUpdated = System.currentTimeMillis();
        }
    }
}
