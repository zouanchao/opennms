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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycle;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;

public class ChronicleQueueWrapper {

    // To be modified as karaf property and set it to /karaf/data/
    public final String queuedir = System.getProperty("org.opennms.core.chronicle.queue.filedir", "/temp");

    private ConcurrentHashMap<String, ExcerptAppender> appenders = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, ExcerptTailer> tailers = new ConcurrentHashMap<>();

    private RollCycle rollCycle = RollCycles.HOURLY;

    private AtomicBoolean closed = new AtomicBoolean(false);

    private List<QueueConsumer> queueConsumers = new ArrayList<>();

    private static final int MAX_MODULES = 10;

    private Semaphore writeCount = new Semaphore(MAX_MODULES);
    
    private AtomicLong size = new AtomicLong();

    public void init() {
        // recover index for all the modules.

    }

    private ExcerptAppender createAppenderForModule(String moduleName) {

        // filePath relative to ModuleName.
        Path queuePath = Paths.get(queuedir, "cq", moduleName);
        ChronicleQueue queue = SingleChronicleQueueBuilder.binary(queuePath).rollCycle(rollCycle).build();
        ExcerptAppender appender = queue.acquireAppender();
        appenders.put(moduleName, appender);
        return appender;
    }

    public void createTailerForModule(String moduleName, QueueDispatcher dispatcher) {
        // Create Tailer and add fileListener for the module.
        Path queuePath = Paths.get(queuedir, "cq", moduleName);
        QueueFileListener fileListener = new QueueFileListener();
        ChronicleQueue readQueue = SingleChronicleQueueBuilder.binary(queuePath).readOnly(true).rollCycle(rollCycle)
                .storeFileListener(fileListener).build();
        ExcerptTailer tailer = readQueue.createTailer();
        long index = getIndexForModule(moduleName);
        if ((index != 0) && !tailer.moveToIndex(index)) {
            // moving to specific Index failed, move to last position.
            tailer.toEnd();
        }
        tailers.put(moduleName, tailer);
        QueueConsumer queueConsumer = new QueueConsumer(tailer, dispatcher);
        Executors.newSingleThreadExecutor().execute(queueConsumer);
    }

    private class QueueConsumer implements Runnable {

        private ExcerptTailer tailer;
        private QueueDispatcher dispatcher;
        private AtomicBoolean closed = new AtomicBoolean(false);

        public QueueConsumer(ExcerptTailer tailer, QueueDispatcher dispatcher) {
            this.tailer = tailer;
            this.dispatcher = dispatcher;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void run() {
            while (!closed.get()) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    if (dc.isPresent()) {
                        Bytes bytes = dc.wire().bytes();
                        long actual = bytes.writePosition() - bytes.readPosition();
                        int length = bytes.length();
                        byte[] output = new byte[length];
                        //System.out.println("read " + length + "bytes  " + actual);
                        bytes.read(output);
                        dispatcher.dispatch(output);
                        //System.out.println("read message from cq");
                        //continue;
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // pass
                        }
                    }
                }

            }
            System.out.println("tailer exited");
        }

        public void stop() {
            closed.set(true);
        }

    }

    @SuppressWarnings("rawtypes")
    public void writeBytesForModule(String moduleName, byte[] data) throws InterruptedException {
        
        ExcerptAppender appender = appenders.get(moduleName);
        if(appender == null) {
            appender = createAppenderForModule(moduleName);
        }
        if (!closed.get()) {
            
            try (DocumentContext dc = appender.writingDocument()) {
                writeCount.tryAcquire();
                Wire wire = dc.wire();
                Bytes bytes = wire.bytes();
                bytes.write(data);
                long len = bytes.writePosition();
                System.out.println("written " + data.length + "bytes");
                System.out.println("write position " + len + "bytes");
                size.addAndGet(data.length);
                //String message = new String(data, StandardCharsets.UTF_8);
            }
            writeCount.release();
        }
    }

    private long getIndexForModule(String moduleName) {
        // TODO read property
        return 0;
    }

    public RollCycle getRollCycle() {
        return rollCycle;
    }

    public void setRollCycle(RollCycle rollCycle) {
        this.rollCycle = rollCycle;
    }

    public void stop() throws InterruptedException {
        // wait till all the writes are finished
        System.out.println("size of queue   " + size);
        while (true) {
            if (writeCount.availablePermits() == MAX_MODULES) {
                break;
            }
            Thread.sleep(100);
        }
        queueConsumers.forEach(consumer -> consumer.stop());
        // store index for all the modules in properties.

    }

}
