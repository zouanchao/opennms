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

package org.opennms.netmgt.correlation.drools;

import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.model.OnmsAlarm;

import com.google.common.collect.Sets;

public class ProblemDomain {

    private final String key;
    private final Set<OnmsAlarm> members = Sets.newLinkedHashSet();

    public ProblemDomain(String key, OnmsAlarm member) {
        this.key = Objects.requireNonNull(key);
        this.members.add(Objects.requireNonNull(member));
    }

    public String getKey() {
        return key;
    }

    public Set<OnmsAlarm> getMembers() {
        return members;
    }

    public void addMember(OnmsAlarm member) {
        members.add(member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ProblemDomain)) {
            return false;
        }
        ProblemDomain other = (ProblemDomain) obj;
        return Objects.equals(key, other.key);
    }

    @Override
    public String toString() {
        return String.format("ProblemDomain[key=%s, members=%s]",
                key, members);
    }
}
