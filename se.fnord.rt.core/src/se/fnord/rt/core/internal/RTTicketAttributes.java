/*
 * Copyright (c) 2010 Henrik Gustafsson <henrik.gustafsson@fnord.se>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package se.fnord.rt.core.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;

import se.fnord.rt.core.internal.attributes.DateParser;
import se.fnord.rt.core.internal.attributes.IdParser;
import se.fnord.rt.core.internal.attributes.IntegerParser;
import se.fnord.rt.core.internal.attributes.RTAttributeParser;
import se.fnord.rt.core.internal.attributes.StringPassthrough;

public enum RTTicketAttributes {
    ID("id", TaskAttribute.TASK_KEY, TaskAttribute.TYPE_INTEGER, null, new IdParser("ticket")),

    QUEUE("Queue", "rt.queue", TaskAttribute.TYPE_SHORT_TEXT, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),
    SUBJECT("Subject", TaskAttribute.SUMMARY, TaskAttribute.TYPE_SHORT_TEXT, null, new StringPassthrough()),
    STATUS("Status", TaskAttribute.STATUS, TaskAttribute.TYPE_SHORT_TEXT, null, new StringPassthrough()),

    PRIORITY("Priority", TaskAttribute.PRIORITY, TaskAttribute.TYPE_INTEGER, TaskAttribute.KIND_DEFAULT, new IntegerParser()),
    INITIAL_PRIORITY("InitialPriority", "rt.initialPriority", TaskAttribute.TYPE_INTEGER, TaskAttribute.KIND_DEFAULT, new IntegerParser()),
    FINAL_PRIORITY("FinalPriority", "rt.finalPriority", TaskAttribute.TYPE_INTEGER, TaskAttribute.KIND_DEFAULT, new IntegerParser()),

    OWNER("Owner", TaskAttribute.USER_ASSIGNED, TaskAttribute.TYPE_PERSON, null, new StringPassthrough()),
    CREATOR("Creator", TaskAttribute.USER_REPORTER, TaskAttribute.TYPE_PERSON, null, new StringPassthrough()),
    REQUESTORS("Requestors", "rt.requestors", TaskAttribute.TYPE_PERSON, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),
    CC("Cc", TaskAttribute.USER_CC, TaskAttribute.TYPE_PERSON, null, new StringPassthrough()),
    ADMIN_CC("AdminCc", "rt.adminCc", TaskAttribute.TYPE_PERSON, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),

    CREATED("Created", TaskAttribute.DATE_CREATION, TaskAttribute.TYPE_DATETIME, null, new DateParser()),
    STARTS("Starts", "rt.dateStarts", TaskAttribute.TYPE_DATETIME, TaskAttribute.KIND_DEFAULT, new DateParser()),
    STARTED("Started", "rt.dateStarted", TaskAttribute.TYPE_DATETIME, TaskAttribute.KIND_DEFAULT, new DateParser()),
    DUE("Due", TaskAttribute.DATE_DUE, TaskAttribute.TYPE_DATETIME, TaskAttribute.KIND_DEFAULT, new DateParser()),
    RESOLVED("Resolved", TaskAttribute.DATE_COMPLETION, TaskAttribute.TYPE_DATETIME, TaskAttribute.KIND_DEFAULT, new DateParser()),
    TOLD("Told", "rt.dateTold", TaskAttribute.TYPE_DATETIME, TaskAttribute.KIND_DEFAULT, new DateParser()),
    LAST_UPDATED("LastUpdated", TaskAttribute.DATE_MODIFICATION, TaskAttribute.TYPE_DATETIME, null, new DateParser()),

    TIME_ESTIMATED("TimeEstimated", "rt.timeEstimated", TaskAttribute.TYPE_SHORT_TEXT, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),
    TIME_WORKED("TimeWorked", "rt.timeWorked", TaskAttribute.TYPE_SHORT_TEXT, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),
    TIME_LEFT("TimeLeft", "rt.timeLeft", TaskAttribute.TYPE_SHORT_TEXT, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),

    COMPONENT("CF.{Component}", TaskAttribute.COMPONENT, TaskAttribute.TYPE_SHORT_TEXT, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),
    ;

    private final RTAttributeParser<?> parser;
    private final String type;
    private final String id;
    private final String name;
    private final String kind;

    private static final Map<String, RTTicketAttributes> nameToObject;
    private static final Map<String, RTTicketAttributes> idToObject;

    static {
        nameToObject = new HashMap<String, RTTicketAttributes>();
        for (RTTicketAttributes attribute : values())
            nameToObject.put(attribute.getName(), attribute);
        idToObject = new HashMap<String, RTTicketAttributes>();
        for (RTTicketAttributes attribute : values())
            idToObject.put(attribute.getId(), attribute);
    }

    private RTTicketAttributes(String name, String id, String type, String kind, RTAttributeParser<?> parser) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.kind = kind;
        this.parser = parser;
    }

    private String getId() {
        return id;
    }

    public static RTTicketAttributes getByName(String name) {
        return nameToObject.get(name);
    }

    public static RTTicketAttributes getById(String id) {
        return idToObject.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T> T parse(String value) {
        return (T) parser.parse(value);
    }

    @SuppressWarnings("unchecked")
    public <T> String dump(T value) {
        return parser.dump(value);
    }

    public TaskAttribute createAttribute(TaskAttributeMapper mapper, TaskAttribute parent, Object data) {
        TaskAttribute attr = parent.createAttribute(id);
        TaskAttributeMetaData metaData = attr.getMetaData();
        metaData.setType(type);
        metaData.setLabel(getName());
        metaData.setKind(kind);

        if (TaskAttribute.TYPE_INTEGER.equals(type))
            mapper.setIntegerValue(attr, (Integer) data);
        else if (TaskAttribute.TYPE_DATETIME.equals(type))
            mapper.setDateValue(attr, (Date) data);
        else if (TaskAttribute.TYPE_SHORT_TEXT.equals(type) || TaskAttribute.TYPE_LONG_TEXT.equals(type) || TaskAttribute.TYPE_LONG_RICH_TEXT.equals(type) || TaskAttribute.TYPE_SHORT_RICH_TEXT.equals(type))
            mapper.setValue(attr, (String) data);
        else if (TaskAttribute.TYPE_PERSON.equals(type))
            mapper.setValue(attr, (String) data);
        else if (TaskAttribute.TYPE_BOOLEAN.equals(type))
            mapper.setBooleanValue(attr, (Boolean) data);

        return attr;
    }

    public static TaskAttribute createDefaultAttribute(TaskAttributeMapper mapper, TaskAttribute parent, String name, Object data) {
        TaskAttribute attr = parent.createAttribute("rt.attribute."+name);

        TaskAttributeMetaData metaData = attr.getMetaData();
        metaData.setType(TaskAttribute.TYPE_SHORT_TEXT);
        metaData.setLabel(name);
        metaData.setKind(TaskAttribute.KIND_DEFAULT);

        mapper.setValue(attr, (String) data);
        return attr;
    }

    public Object createObject(TaskAttributeMapper mapper, TaskAttribute attribute) {

        if (TaskAttribute.TYPE_INTEGER.equals(type))
            return mapper.getIntegerValue(attribute);
        else if (TaskAttribute.TYPE_DATETIME.equals(type))
            return mapper.getDateValue(attribute);
        else if (TaskAttribute.TYPE_SHORT_TEXT.equals(type) || TaskAttribute.TYPE_LONG_TEXT.equals(type) || TaskAttribute.TYPE_LONG_RICH_TEXT.equals(type) || TaskAttribute.TYPE_SHORT_RICH_TEXT.equals(type))
            return mapper.getValue(attribute);
        else if (TaskAttribute.TYPE_PERSON.equals(type))
            return mapper.getRepositoryPerson(attribute).getPersonId();
        else if (TaskAttribute.TYPE_BOOLEAN.equals(type))
            mapper.getBooleanValue(attribute);
        throw new IllegalArgumentException("Unhandled type");
    }

    public String getName() {
        return name;
    }

}