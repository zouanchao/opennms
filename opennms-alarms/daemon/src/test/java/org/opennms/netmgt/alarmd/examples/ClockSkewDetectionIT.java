/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.examples;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.time.PseudoClock;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.alarmd.drools.DefaultAlarmService;
import org.opennms.netmgt.alarmd.drools.DroolsAlarmContext;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.dao.support.AlarmEntityNotifierImpl;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/emptyContext.xml"
})
@JUnitConfigurationEnvironment
public class ClockSkewDetectionIT {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private DroolsAlarmContext dac;
    private AlarmDao alarmDao;
    private EventForwarder eventForwarder;
    private AtomicInteger alarmIdGenerator = new AtomicInteger();

    /**
     * FIXME: Remove boilerplate
     * @throws IOException
     */
    @Before
    public void setUp() throws IOException {
        // Copy default set of rules to a new folder
        File rulesFolder = temporaryFolder.newFolder("rules");
        FileUtils.copyDirectory(DroolsAlarmContext.getDefaultRulesFolder(), rulesFolder);

        // Copy the rules from the example folder alongside the rules in our temporary folder
        FileUtils.copyFile(Paths.get(ConfigFileConstants.getHome(), "etc", "examples",
                "alarmd", "drools-rules.d", "clock-skew.drl").toFile(),
                new File(rulesFolder, "clock-skew.drl"));

        // Wire up the engine with mocks
        dac = new DroolsAlarmContext(rulesFolder);
        dac.setUsePseudoClock(true);
        dac.setUseManualTick(true);

        MockTransactionTemplate transactionTemplate = new MockTransactionTemplate();
        transactionTemplate.afterPropertiesSet();
        dac.setTransactionTemplate(transactionTemplate);

        alarmDao = mock(AlarmDao.class);
        dac.setAlarmDao(alarmDao);

        DefaultAlarmService alarmService = new DefaultAlarmService();
        alarmService.setAlarmDao(alarmDao);
        eventForwarder = mock(EventForwarder.class);
        alarmService.setEventForwarder(eventForwarder);

        AcknowledgmentDao acknowledgmentDao = mock(AcknowledgmentDao.class);
        when(acknowledgmentDao.findLatestAckForRefId(any(Integer.class))).thenReturn(Optional.empty());
        alarmService.setAcknowledgmentDao(acknowledgmentDao);

        AlarmEntityNotifierImpl alarmEntityNotifier = mock(AlarmEntityNotifierImpl.class);
        alarmService.setAlarmEntityNotifier(alarmEntityNotifier);
        dac.setAlarmService(alarmService);
        dac.setAcknowledgmentDao(acknowledgmentDao);

        dac.start();
        dac.waitUntilStarted();
    }

    public class SimulatedNode {
        private final int nodeId;
        private final long clockSkewInMs;
        private final List<OnmsAlarm> alarms = new ArrayList<>();

        public SimulatedNode(int nodeId, long clockSkewInMs) {
            this.nodeId = nodeId;
            this.clockSkewInMs = clockSkewInMs;
        }

        public void triggerLinkDownAlarms() {
            OnmsAlarm syslogLinkDown = createLinkDownAlarm("syslogd", true);
            when(alarmDao.get(syslogLinkDown.getId())).thenReturn(syslogLinkDown);
            dac.handleNewOrUpdatedAlarm(syslogLinkDown);
            alarms.add(syslogLinkDown);

            OnmsAlarm trapLinkDown = createLinkDownAlarm("trapd", false);
            when(alarmDao.get(trapLinkDown.getId())).thenReturn(trapLinkDown);
            dac.handleNewOrUpdatedAlarm(trapLinkDown);
            alarms.add(trapLinkDown);
        }

        public void deleteLinkDownAlarms() {
            for (OnmsAlarm alarm : alarms) {
                dac.handleDeletedAlarm(alarm.getId(), alarm.getReductionKey());
            }
            alarms.clear();
        }

        private OnmsAlarm createLinkDownAlarm(String source, boolean applySkew) {
            final long now = PseudoClock.getInstance().getTime();

            final OnmsEvent event = new OnmsEvent();
            event.setEventSource(source);
            event.setEventCreateTime(new Date(now));
            if (applySkew) {
                event.setEventTime(new Date(now - clockSkewInMs));
            } else {
                event.setEventTime(new Date(now));
            }

            final int alarmId = alarmIdGenerator.incrementAndGet();
            final OnmsAlarm alarm = new OnmsAlarm();
            alarm.setId(alarmId);
            alarm.setAlarmType(1);
            alarm.setSeverity(OnmsSeverity.WARNING);
            alarm.setReductionKey("uei.opennms.org/linkDown/alarm:" + alarmId);
            alarm.setFirstEventTime(event.getEventTime());
            alarm.setLastEvent(event);

            final OnmsNode node = mock(OnmsNode.class);
            when(node.getId()).thenReturn(nodeId);
            alarm.setNode(node);

            when(alarmDao.get(alarm.getId())).thenReturn(alarm);
            return alarm;
        }
    }

    @Test
    public void canDetectClockSkew() {
        // Simulate two nodes, one with a clock skew and one without
        SimulatedNode n1 = new SimulatedNode(1, 0);
        SimulatedNode n2 = new SimulatedNode(2, TimeUnit.SECONDS.toMillis(31));

        // Advance the time
        dac.getClock().advanceTime( 60, TimeUnit.SECONDS );
        PseudoClock.getInstance().advanceTime(60, TimeUnit.SECONDS);

        // Trigger link down alarms on each node
        n1.triggerLinkDownAlarms();
        n2.triggerLinkDownAlarms();
        dac.tick();

        // No events should have been generated
        verify(eventForwarder, times(0)).sendNow(any(Event.class));

        // Advance the time
        dac.getClock().advanceTime( 60, TimeUnit.SECONDS );
        PseudoClock.getInstance().advanceTime(60, TimeUnit.SECONDS);

        // Delete the link down alarms
        n1.deleteLinkDownAlarms();
        n2.deleteLinkDownAlarms();
        dac.tick();

        // No events should have been generated
        verify(eventForwarder, times(0)).sendNow(any(Event.class));

        // Advance the time 30 minutes
        dac.getClock().advanceTime( 30, TimeUnit.MINUTES );
        PseudoClock.getInstance().advanceTime(30, TimeUnit.MINUTES);

        // Trigger link down alarms on each node
        n1.triggerLinkDownAlarms();
        n2.triggerLinkDownAlarms();
        dac.tick();

        // Verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventForwarder).sendNow(eventCaptor.capture());
        Event event = eventCaptor.getValue();
        assertThat(event.getUei(), equalTo("uei.opennms.org/nodes/clockSkewDetected"));
        reset(eventForwarder);

        // Advance the time 30 minutes
        dac.getClock().advanceTime( 30, TimeUnit.MINUTES );
        PseudoClock.getInstance().advanceTime(30, TimeUnit.MINUTES);
        dac.tick();

        // No new events should have been generated
        verify(eventForwarder, times(0)).sendNow(any(Event.class));
    }
}
