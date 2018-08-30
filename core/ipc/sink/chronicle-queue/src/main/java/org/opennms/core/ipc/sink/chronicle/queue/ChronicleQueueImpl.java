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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.core.ipc.sink.api.OffHeapFifoQueue;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycle;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.StoreFileListener;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;

public class ChronicleQueueImpl implements OffHeapFifoQueue {

    public final String queuedir = System.getProperty("org.opennms.core.chronicle.queue.filedir", "/temp");
    private RollCycle rollCycle = RollCycles.HOURLY;
    private ExcerptAppender appender;
    private ExcerptTailer tailer;
    private AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void init(int maxSizeInBytes) {
        //TODO: Add moduleName specific to this queue.
        Path queuePath = Paths.get(queuedir);
        ChronicleQueue queue = SingleChronicleQueueBuilder.binary(queuePath).rollCycle(rollCycle).build();
        appender = queue.acquireAppender();
        queue = SingleChronicleQueueBuilder.binary(queuePath).rollCycle(rollCycle)
                .storeFileListener(new QueueFileListener()).build();
        tailer = queue.createTailer();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void writeMessage(byte[] message) {
        try (DocumentContext dc = appender.writingDocument()) {
            Wire wire = dc.wire();
            Bytes bytes = wire.bytes();
            bytes.write(message);
        }

    }

    @SuppressWarnings("rawtypes")
    @Override
    public byte[] readNextMessage() throws InterruptedException {
        while (!closed.get()) {
            try (DocumentContext dc = tailer.readingDocument()) {
                if (dc.isPresent()) {
                    Bytes bytes = dc.wire().bytes();
                    int length = bytes.length();
                    byte[] output = new byte[length];
                    bytes.read(output);
                    return output;
                } else {
                    Thread.sleep(1000);
                }
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        closed.set(true);
    }

    private class QueueFileListener implements StoreFileListener {

        @Override
        public void onReleased(int cycle, File file) {
            System.out.println("onReleased(): cycle = " + cycle + "   " + file);
            // File listener is only registered on read queue, so it can be
            // deleted once it is read.
            int currentCycle = -1;
            if (tailer != null) {
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

    }
}
