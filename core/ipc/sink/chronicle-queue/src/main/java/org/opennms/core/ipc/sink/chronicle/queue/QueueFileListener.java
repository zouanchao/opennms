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

package org.opennms.core.ipc.sink.chronicle.queue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.StoreFileListener;

public class QueueFileListener implements StoreFileListener {

    private ExcerptTailer tailer;

    @Override
    public void onReleased(int cycle, File file) {
        System.out.println("onReleased(): cycle = " + cycle + "   " + file);
        // File listener is only registered on read queue, so it can be deleted
        // once it is read.
        int currentCycle = -1;
        if(tailer != null) {
            currentCycle = tailer.cycle();
        }
        if (currentCycle > 0 && cycle < currentCycle) {
            try {
                System.out.println("Deleting file " + file);
                Files.delete(file.toPath());
            } catch (IOException e) {
                // Add log here.
            }
        }
    }

    public void setTailer(ExcerptTailer tailer) {
        this.tailer = tailer;
    }

}
