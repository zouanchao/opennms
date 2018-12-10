/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.tags.filters;

import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.web.event.EventUtil;
import org.opennms.web.filter.Filter;

public class EventFilterCallback extends AbstractFilterCallback {

    public EventFilterCallback(ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    protected String getIndividualFilterString(Filter filter) {
        return EventUtil.getFilterString(filter);
    }

    @Override
    protected List<Filter> getIndividualFilterList(String[] filters, ServletContext servletContext) {
        return EventUtil.getFilterList(filters, servletContext);
    }
}
