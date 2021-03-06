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
package se.fnord.rt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;

import se.fnord.rt.client.internal.attributes.LinkParser;
import se.fnord.rt.client.internal.attributes.RTAttributeParser;

public enum RTLinkType {
    DEPENDS_ON("DependsOn", "rt.link.dependsOn"),
    DEPENDED_ON_BY("DependedOnBy", "rt.link.dependedOnBy"),
    REFERS_TO("RefersTo", "rt.link.refersTo"),
    REFERRED_TO_BY("ReferredToBy", "rt.link.referredToBy"),
    MEMBERS("Members", "rt.link.members"),
    MEMBER_OF("MemberOf", "rt.link.memberOf"),
    ;
    public static final String RT_LINK_PREFIX = "rt.link.";
    public static final Pattern SPLITTER = Pattern.compile("\\s*[\\s,]\\s*");

    private static final RTAttributeParser<List<Integer>> LINK_PARSER = new LinkParser();
    private final String id;
    private final String name;

    private static final Map<String, RTLinkType> nameToObject;
    private static final Map<String, RTLinkType> idToObject;

    static {
        nameToObject = new HashMap<String, RTLinkType>();
        for (RTLinkType attribute : values())
            nameToObject.put(attribute.getName(), attribute);
        idToObject = new HashMap<String, RTLinkType>();
        for (RTLinkType attribute : values())
            idToObject.put(attribute.getId(), attribute);
    }

    private RTLinkType(String name, String id) {
        this.name = name;
        this.id = id;
    }

    private String getId() {
        return id;
    }

    public static RTLinkType getByName(String name) {
        return nameToObject.get(name);
    }

    public static RTLinkType getById(String id) {
        return idToObject.get(id);
    }

    public List<Integer> parse(String value) {
        return LINK_PARSER.parse(value);
    }

    public String dump(List<Integer> value) {
        return LINK_PARSER.dump(value);
    }

    public TaskAttribute createAttribute(TaskAttributeMapper mapper, TaskAttribute parent, List<Integer> data) {
        TaskAttribute attr = parent.createAttribute(id);
        TaskAttributeMetaData metaData = attr.getMetaData();
        metaData.setType(TaskAttribute.TYPE_TASK_DEPENDENCY);
        metaData.setLabel(getName());
        metaData.setKind(TaskAttribute.KIND_DEFAULT);
        metaData.setReadOnly(false);

        ArrayList<String> strings = new ArrayList<String>(data.size());
        for (Integer i : data)
            strings.add(i.toString());

        mapper.setValues(attr, strings);

        return attr;
    }

    public List<Integer> createObject(TaskAttributeMapper mapper, TaskAttribute attribute) {
        // TODO: Eh, I set lists and get concatenated stuff back. Very inconsistent.
        final List<String> values = mapper.getValues(attribute);
        final ArrayList<Integer> ints = new ArrayList<Integer>(values.size());
        for (final String s : values) {
            final String[] split = SPLITTER.split(s);
            for (final String ss : split) {
                final String val = ss.trim();
                if (!val.isEmpty())
                    ints.add(Integer.parseInt(val));
            }
        }
        return ints;
    }

    public String getName() {
        return name;
    }
}
