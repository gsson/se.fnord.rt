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
import se.fnord.rt.core.internal.attributes.IntegerParser;
import se.fnord.rt.core.internal.attributes.RTAttributeParser;
import se.fnord.rt.core.internal.attributes.StringPassthrough;


public enum RTHistoryAttributes {
    ID("id", TaskAttribute.COMMENT_NUMBER, TaskAttribute.TYPE_INTEGER, null, new IntegerParser()),

    TYPE("Type", "rt.commentType", TaskAttribute.TYPE_SHORT_TEXT, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),
    CREATOR("Creator", TaskAttribute.COMMENT_AUTHOR, TaskAttribute.TYPE_PERSON, TaskAttribute.KIND_PEOPLE, new StringPassthrough()),
    CREATED_TIME("Created", TaskAttribute.COMMENT_DATE, TaskAttribute.TYPE_DATETIME, TaskAttribute.KIND_DEFAULT, new DateParser()),
    CONTENT("Content", TaskAttribute.COMMENT_TEXT, TaskAttribute.TYPE_LONG_RICH_TEXT, null, new StringPassthrough()),
    ;
    
    private final RTAttributeParser<?> parser;
    private final String type;
    private final String id;
    private final String name;
    private final String kind;
    
    private static final Map<String, RTHistoryAttributes> nameToObject;
    
    static {
        nameToObject = new HashMap<String, RTHistoryAttributes>();
        for (RTHistoryAttributes attribute : values())
            nameToObject.put(attribute.getName(), attribute);
    }

    private RTHistoryAttributes(String name, String id, String type, String kind, RTAttributeParser<?> parser) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.kind = kind;
        this.parser = parser;
    }
    
    public static RTHistoryAttributes getByName(String name) {
        return nameToObject.get(name);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T parse(String value) {
        return (T) parser.parse(value);
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
        else if (TaskAttribute.TYPE_PERSON.equals(type)) {
            mapper.getRepositoryPerson(null);
            mapper.setValue(attr, (String) data);
        }
        else if (TaskAttribute.TYPE_BOOLEAN.equals(type))
            mapper.setBooleanValue(attr, (Boolean) data);
        
        return attr;
    }

    public String getName() {
        return name;
    }
}
