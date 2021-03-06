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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import se.fnord.rt.core.internal.StandardField.OptionImpl;
import se.fnord.rt.core.internal.extfields.Version;

@XmlRootElement(name="fields")
public class StandardFields implements Fields, Serializable {
    private static final long serialVersionUID = 6127237818899938841L;
    private final String version;

    private List<StandardField> fields = Collections.emptyList();
    private transient Map<String, StandardField> fieldsByRTId = null;
    private transient Map<String, StandardField> fieldsByMylynId = null;
    private static final Pattern PATTERN = Pattern.compile("\\.");

    public static final Comparator<String> VERSION_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            String[] split = PATTERN.split(o1);
            String[] split2 = PATTERN.split(o2);
            int n = Math.min(split.length, split2.length);
            for (int i = 0; i < n; i++) {
                try {
                    int diff = Integer.parseInt(split[i]) - Integer.parseInt(split2[i]);
                    if (diff != 0)
                        return diff;
                }
                catch (NumberFormatException e) {
                    int diff = split[i].compareToIgnoreCase(split2[i]);
                    if (diff != 0)
                        return diff;
                }
            }
            int diff = split.length - split2.length;
            if (diff != 0)
                return diff;
            return 0;
        }
    };

    private List<Option> queues;

    public StandardFields(final String version, final Collection<QueueInfo> queueInfo) {
        this.version = version;
        this.queues = new ArrayList<Option>();
        for (QueueInfo info : queueInfo)
            this.queues.add(new OptionImpl(info.getName(), info.getName()));
    }

    public void load() throws FileNotFoundException, IOException {
        /* TODO: Yea...do something proper here */

        final String resourcePath = "/se/fnord/rt/core/standardfields.xml";
        fieldsByMylynId = null;
        fieldsByRTId = null;

        try {
            JAXBContext ctx = JAXBContext.newInstance(se.fnord.rt.core.internal.extfields.Fields.class);
            Unmarshaller um = ctx.createUnmarshaller();
            se.fnord.rt.core.internal.extfields.Fields f = (se.fnord.rt.core.internal.extfields.Fields) um.unmarshal(getClass().getResourceAsStream(resourcePath));

            for (Version v: f.versions) {
                if ((v.minVersion == null || VERSION_COMPARATOR.compare(v.minVersion, version) <= 0) && (v.maxVersion == null || VERSION_COMPARATOR.compare(version, v.maxVersion) <= 0)) {
                    final ArrayList<StandardField> newFields = new ArrayList<StandardField>(v.fields.size());

                    /* Replace queue field with one populated with the configured queues as options */
                    for (StandardField field : v.fields) {
                        if ("rt.fields.queue".equals(field.getMylynId()))
                            newFields.add(new StandardField(field.getMylynId(), field.getRTId(), field.getLabel(), field.getDescription(), field.getKind(), field.getType(), field.getTranslatorName(), field.isReadOnly(), queues));
                        else
                            newFields.add(field);
                    }
                    this.fields = Collections.unmodifiableList(newFields);
                    return;
                }
            }
        } catch (JAXBException e) {
            /* TODO: Error handling that notifies users when something is broken */
            throw new RuntimeException(e);
        }
        throw new RuntimeException(String.format("No fields for version %s found!", version));
    }

    private void buildMaps() {
        this.fieldsByRTId = new HashMap<String, StandardField>();
        this.fieldsByMylynId = new HashMap<String, StandardField>();
        for (final StandardField field : fields) {
            fieldsByRTId.put(field.getRTId(), field);
            fieldsByMylynId.put(field.getMylynId(), field);
        }
    }

    @Override
    public List<StandardField> getFields() {
        return fields;
    }

    @Override
    public StandardField getByRTId(final String name) {
        if (fieldsByRTId == null)
            buildMaps();
        return fieldsByRTId.get(name);
    }

    @Override
    public StandardField getByMylynId(final String name) {
        if (fieldsByMylynId == null)
            buildMaps();
        return fieldsByMylynId.get(name);
    }
}
