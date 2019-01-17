/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.features.es.alarms;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Here we store the search queries in the class-path in the form
 * of Freemarker templates and use the templating to perform the parameter
 * substitution.
 *
 * This makes it cleaner than storing the search queries as multiline strings
 * in Java code, and is much less verbose than storing these as POJOs.
 */
public class QueryProvider {

    private static final int NUM_MAX_ALARMS = 1000;

    private final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    public QueryProvider() {
        // Setup Freemarker
        cfg.setClassForTemplateLoading(getClass(), "");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String getAlarmAt(int id, long time) {
        return render("get_alarm_at.ftl", ImmutableMap.builder()
                .put("alarmId", id)
                .put("time", time)
                .build());
    }

    public String getAlarmsAt(long time) {
        return render("get_alarms_at.ftl", ImmutableMap.builder()
                .put("time", time)
                .put("numMaxAlarms", NUM_MAX_ALARMS)
                .build());
    }

    public String getAllAlarms() {
        return render("get_all_alarms.ftl", ImmutableMap.builder()
                .put("numMaxAlarms", NUM_MAX_ALARMS)
                .build());
    }

    public String getCurrentAlarms() {
        return render("get_current_alarms.ftl", ImmutableMap.builder()
                .put("numMaxAlarms", NUM_MAX_ALARMS)
                .build());
    }

    public String markAlarmAsDeleted(int alarmId, long deletedTime) {
        return render("mark_alarm_as_deleted.ftl", ImmutableMap.builder()
                .put("alarmId", alarmId)
                .put("deletedTime", deletedTime)
                .build());
    }

    public String getActiveAlarmsAtTimeAndExclude(long time, Set<Integer> alarmIdsToKeep) {
        return render("get_active_alarms_at_time_and_exclude.ftl", ImmutableMap.builder()
                .put("time", time)
                .put("alarmIds", alarmIdsToKeep)
                .put("numMaxAlarms", NUM_MAX_ALARMS)
                .build());
    }

    public String markOtherAlarmsAsDeleted(Set<Integer> alarmIdsToKeep, long deletedTime) {
        return render("mark_other_alarms_as_deleted.ftl", ImmutableMap.builder()
                .put("alarmIds", alarmIdsToKeep)
                .put("deletedTime", deletedTime)
                .build());
    }

    private String render(String templateName, Map<Object, Object> context) {
        try {
            final StringWriter writer = new StringWriter();
            final Template template = cfg.getTemplate(templateName);
            template.process(context, writer);
            return writer.toString();
        } catch (IOException|TemplateException e) {
            throw new RuntimeException(e);
        }
    }



}
