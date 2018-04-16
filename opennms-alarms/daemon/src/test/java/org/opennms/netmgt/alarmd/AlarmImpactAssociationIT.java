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

package org.opennms.netmgt.alarmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase=false)
public class AlarmImpactAssociationIT {

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private TransactionTemplate m_transactionTemplate;

    @Test
    public void canAssociateImpactingAlarms() {
        // Create a first alarms
        final OnmsAlarm linkDownAlarmOnR1 = new OnmsAlarm();
        linkDownAlarmOnR1.setDistPoller(m_distPollerDao.whoami());
        linkDownAlarmOnR1.setCounter(1);
        linkDownAlarmOnR1.setUei("linkDown");

        // Create a second alarm
        final OnmsAlarm linkDownAlarmOnR2 = new OnmsAlarm();
        linkDownAlarmOnR2.setDistPoller(m_distPollerDao.whoami());
        linkDownAlarmOnR2.setCounter(1);
        linkDownAlarmOnR2.setUei("linkDown");

        // Save them and associate them together in one transaction
        m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                m_alarmDao.save(linkDownAlarmOnR1);
                verifyInitialState(linkDownAlarmOnR1);

                // Associate
                associate(linkDownAlarmOnR1, linkDownAlarmOnR2);

                // Verify
                verifyAssociation(linkDownAlarmOnR1, linkDownAlarmOnR2);
            }
        });

        m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                // Now reload the entities
                OnmsAlarm r1 = m_alarmDao.get(linkDownAlarmOnR1.getId());
                OnmsAlarm r2 = m_alarmDao.get(linkDownAlarmOnR2.getId());

                // And verify again
                verifyAssociation(r1, r2);
            }
        });
    }

    void associate(OnmsAlarm cause, OnmsAlarm impacted) {
        List<OnmsAlarm> impacts = cause.getImpacts();
        impacts.add(impacted);
        cause.setImpacts(impacts);
        List<OnmsAlarm> causes = impacted.getCauses();
        causes.add(cause);
        impacted.setCauses((causes));
        m_alarmDao.saveOrUpdate(cause);
        m_alarmDao.saveOrUpdate(impacted);
    }

    void verifyInitialState(OnmsAlarm alarm) {
        assertFalse("cause should default to false", alarm.isCause());
        assertFalse("impacted should default to false", alarm.isImpacted());
        assertEquals(0, alarm.getImpacts().size());
        assertEquals(0, alarm.getCauses().size());
    }

    void verifyAssociation(OnmsAlarm cause, OnmsAlarm impacted) {
        assertEquals(1, cause.getImpacts().size());
        assertEquals(0, cause.getCauses().size());
        assertTrue(cause.isCause());
        assertFalse(cause.isImpacted());

        assertEquals(0, impacted.getImpacts().size());
        assertEquals(1, impacted.getCauses().size());
        assertFalse(impacted.isCause());
        assertTrue(impacted.isImpacted());
    }
}