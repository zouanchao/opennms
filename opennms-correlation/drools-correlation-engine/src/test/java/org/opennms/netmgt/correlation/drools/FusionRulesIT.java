/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.alarmd.Alarmd;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/correlation-engine.xml",
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml",
        "classpath:/test-context.xml"
})
@JUnitConfigurationEnvironment(systemProperties={"org.opennms.activemq.broker.disable=true"})
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass= MockDatabase.class,reuseDatabase=false)
public class FusionRulesIT implements TemporaryDatabaseAware<MockDatabase>, InitializingBean {


    @Autowired
    private MockEventIpcManager m_eventIpcMgr;

    @Autowired
    private CorrelationEngineRegistrar m_correlator;

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private Alarmd m_alarmd;

    private MockDatabase m_database;

    protected Integer m_anticipatedMemorySize = 0;


    public FusionRulesIT() {
        ConfigurationTestUtils.setRelativeHomeDirectory("src/test/opennms-home");
    }

    @Before
    public void setup() {
        m_eventIpcMgr.setEventWriter(m_database);
    }

    @Override
    public void afterPropertiesSet() {
        BeanUtils.assertAutowiring(this);
    }

    public void setCorrelationEngineRegistrar(CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
    }

    @Test
    public void testDroolsFusion() throws Exception {

        anticipate(createNodeUpEvent(1));
        anticipate(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "droolsFusion").getEvent());
        anticipate(new EventBuilder(EventConstants.DROOLS_ENGINE_ENCOUNTERED_EXCEPTION, "droolsFusion").getEvent());
        EventBuilder eb = new EventBuilder("uei.opennms.org/triggerTestForFusion", "droolsTest");
        DroolsCorrelationEngine engine = findEngineByName("droolsFusion");
        // This will create an exception and the engine should reload.
        engine.correlate(eb.getEvent());
        Thread.sleep(10000); // Wait time for the engine to reload.
        // The engine should run other rules after reload.
        engine.correlate(createNodeLostServiceEvent(1, "SSH"));
        Thread.sleep(4000); // Give time to execute the time-based rules.
        m_anticipatedMemorySize = 5;
        engine.getKieSessionObjects().forEach(System.err::println);
        verify(engine);
        OnmsAlarm alarm = m_alarmDao.findByReductionKey(EventConstants.DROOLS_ENGINE_ENCOUNTERED_EXCEPTION + ":" + "droolsFusion");
        assertNotNull(alarm);

    }

    public Event createNodeDownEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_DOWN_EVENT_UEI, nodeid);
    }
    
    public Event createNodeUpEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_UP_EVENT_UEI, nodeid);
    }

    private Event createNodeEvent(String uei, int nodeid) {
        return new EventBuilder(uei, "test")
            .setNodeid(nodeid)
            .getEvent();
    }

    private Event createNodeLostServiceEvent(int nodeid, String serviceName) {
        return new EventBuilder(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, serviceName)
            .setNodeid(nodeid)
            .getEvent();
    }

    protected DroolsCorrelationEngine findEngineByName(String engineName) {
        return (DroolsCorrelationEngine) m_correlator.findEngineByName(engineName);
    }

    protected void anticipate(Event event) {
        getAnticipator().anticipateEvent(event);
    }

    protected void verify(DroolsCorrelationEngine engine) {
        getAnticipator().verifyAnticipated(2000, 0, 0, 0, 0);
        if (m_anticipatedMemorySize != null) {
            assertEquals("Unexpected number of objects in working memory: "+engine.getKieSessionObjects(), m_anticipatedMemorySize.intValue(), engine.getKieSessionObjects().size());
        }
    }
    protected EventAnticipator getAnticipator() {
        return m_eventIpcMgr.getEventAnticipator();
    }

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_database  = database;
    }
}
