//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.report.datablock;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * This contains a list of service lost/regained set/pair. 
 * 
 *  Also maintains the outage/down time each time it is calculated and the time
 *  from which this was calculated - this is done so when the view outage time for
 *  a window is calculated, the same calculations are not done on the node multiple
 *  times
 * 
 *  @author 	&lt;A HREF=&quot;mailto:jacinta@oculan.com&quot;&gt;Jacinta Remedios&lt;/A&gt;
 *  @author	&lt;A HREF=&quot;http://www.oculan.com&quot;&gt;oculan.org&lt;/A&gt;
 * 
 * 
 */
public class OutageSvcTimesList extends ArrayList<Outage> {
    /**
     * 
     */
    private static final long serialVersionUID = -4701288568774571119L;

    /**
     * The time from which the current outtime 'm_outTime' is calculated
     */
    @SuppressWarnings("unused")
    private long m_outTimeSince;

    /**
     * The outage time computed since 'm_outTimeSince'
     */
    private long m_outTime;

    /**
     * The outage time computed during business hours.
     */
    @SuppressWarnings("unused")
    private long m_busOutTime;

    /**
     * Default constructor
     * 
     * @see java.util.ArrayList#ArrayList()
     */
    public OutageSvcTimesList() {
        super();

        m_outTimeSince = -1;

        m_outTime = 0;
    }

    /**
     * Constructor
     * 
     * @see java.util.ArrayList#ArrayList(int initCapacity)
     */
    public OutageSvcTimesList(int initialCapacity) {
        super(initialCapacity);

        m_outTimeSince = -1;

        m_outTime = 0;
    }

    /**
     * Add a new servicetime entry
     * 
     * @param losttime
     *            time at which service was lost
     * @param regainedtime
     *            time at which service was regained
     */
    public void addSvcTime(long losttime, long regainedtime) {
        if (regainedtime < losttime)
            return;

        add(new Outage(losttime, regainedtime));
    }

    /**
     * Add a new service time entry
     * 
     * @param losttime
     *            time at which service was lost
     */
    public void addSvcTime(long losttime) {
        add(new Outage(losttime));
    }

    /**
     * Calculate the total downtime in this list of service times for the last
     * 'rollinWindow' time starting at 'curTime'
     * 
     * @param curTime
     *            the current time from which the down time is to be calculated
     * @param rollingWindow
     *            the last window for which the downtime is to be calculated
     * 
     * @return total down time in service times in this list
     */
    public long getDownTime(long curTime, long rollingWindow) {
        // calculate effective start time
        long startTime = curTime - rollingWindow;
        m_outTimeSince = startTime;

        m_outTime = 0;

        for(Outage svcTime : this) {
            long outtime = svcTime.getDownTime(curTime, rollingWindow);
            if (outtime > 0)
                m_outTime += outtime;
        }

        return m_outTime;
    }

    /**
     * Returns a list of outage / out-since pairs for the rolling window
     * specified
     * 
     * @param curTime
     *            the current time from which the down time is to be calculated
     * @param rollingWindow
     *            the last window for which the down time is to be calculated
     * 
     */
    public List<OutageSince> getServiceOutages(String nodeName, long curTime, long rollingWindow) {
        if (nodeName == null)
            return null;

        // for each individual outage, get the down time
        // 
        // calculate effective start time
        long startTime = curTime - rollingWindow;
        List<OutageSince> retList = new ArrayList<OutageSince>();

        for(Outage svcTime : this) {

            // ignore if the outage doesn't fall within the window.
            //
            if (svcTime.getRegainedTime() > 0) {
                if (svcTime.getRegainedTime() <= startTime)
                    continue;
            } else {
                if (svcTime.getLostTime() > curTime)
                    continue;
            }

            long outFrom = startTime;
            if (startTime < svcTime.getLostTime()) {
                outFrom = svcTime.getLostTime();
            }
            long outtime = svcTime.getDownTime(curTime, rollingWindow);
            OutageSince outageSince = null;
            if (outtime > 0)
                outageSince = new OutageSince(nodeName, outFrom, outtime);
            if (outageSince != null)
                retList.add(outageSince);
        }

        return retList;
    }

    public String toString() {
        String retVal = "";
        for(Outage outage : this) {
            retVal += "\n" + outage;
        }
        return retVal;
    }
}
