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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import se.fnord.rt.core.internal.attributes.IdParser;
import se.fnord.rt.core.internal.attributes.IntegerParser;
import se.fnord.rt.core.internal.attributes.RTAttributeParser;
import se.fnord.rt.core.internal.attributes.StringPassthrough;

public enum RTUserAttributes {
    ID("id", "rt.person.id", TaskAttribute.TYPE_INTEGER, null, new IdParser("user")),

    NAME("Name", "rt.person.username", TaskAttribute.TYPE_SHORT_TEXT, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),
    REAL_NAME("RealName", TaskAttribute.PERSON_NAME, TaskAttribute.KIND_DEFAULT, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),
    EMAIL("EmailAddress", "rt.person.email", TaskAttribute.TYPE_SHORT_TEXT, TaskAttribute.KIND_DEFAULT, new StringPassthrough()),
    NICK("NickName", "rt.person.nick", TaskAttribute.TYPE_SHORT_TEXT, TaskAttribute.KIND_PEOPLE, new StringPassthrough()),
    DISABLED("Disabled", "rt.person.disabled", TaskAttribute.TYPE_INTEGER, null, new IntegerParser())
    ;
    
    private final RTAttributeParser<?> parser;
    private final String type;
    private final String id;
    private final String name;
    private final String kind;
    
    private static final Map<String, RTUserAttributes> nameToObject;
    
    static {
        nameToObject = new HashMap<String, RTUserAttributes>();
        for (RTUserAttributes attribute : values())
            nameToObject.put(attribute.getName(), attribute);
    }

    private RTUserAttributes(String name, String id, String type, String kind, RTAttributeParser<?> parser) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.kind = kind;
        this.parser = parser;
    }
    
    public static RTUserAttributes getByName(String name) {
        return nameToObject.get(name);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T parse(String value) {
        return (T) parser.parse(value);
    }

    public String getName() {
        return name;
    }
}
