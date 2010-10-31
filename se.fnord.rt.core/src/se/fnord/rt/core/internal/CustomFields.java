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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomFields implements Fields, Serializable {
    private static final long serialVersionUID = 5752535170103086409L;
    private final List<CustomField> fields;
    private transient Map<String, CustomField> fieldsByRTId = null;
    private transient Map<String, CustomField> fieldsByMylynId = null;

    public CustomFields(final List<CustomField> fields) {
        this.fields = Collections.unmodifiableList(new ArrayList<CustomField>(fields));
    }

    @Override
    public CustomField getByRTId(final String rtId) {
        if (fieldsByRTId == null)
            buildMaps();
        return fieldsByRTId.get(rtId);
    }

    @Override
    public CustomField getByMylynId(final String mylynId) {
        if (fieldsByMylynId == null)
            buildMaps();
        return fieldsByMylynId.get(mylynId);
    }

    private void buildMaps() {
        fieldsByMylynId = new HashMap<String, CustomField>(fields.size());
        fieldsByRTId = new HashMap<String, CustomField>(fields.size());
        for (CustomField field : fields) {
            fieldsByMylynId.put(field.getMylynId(), field);
            fieldsByRTId.put(field.getRTId(), field);
        }
    }

    @Override
    public List<CustomField> getFields() {
        return fields;
    }

}
