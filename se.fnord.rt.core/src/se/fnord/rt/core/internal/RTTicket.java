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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RTTicket {
    public final int taskId;
    public final Map<RTTicketAttributes, Object> fields;
    public final List<RTHistory> comments;
    public final boolean partial;

    public RTTicket(final Map<RTTicketAttributes, Object> fields) {
        this.fields = new EnumMap<RTTicketAttributes, Object>(RTTicketAttributes.class);
        this.fields.putAll(fields);
        this.comments = null;
        this.taskId = (Integer) fields.get(RTTicketAttributes.ID);
        this.partial = true;
    }

    public RTTicket(final Map<RTTicketAttributes, Object> fields, List<RTHistory> comments) {
        this.fields = new EnumMap<RTTicketAttributes, Object>(RTTicketAttributes.class);
        this.fields.putAll(fields);
        this.comments = new ArrayList<RTHistory>(comments);
        this.taskId = (Integer) fields.get(RTTicketAttributes.ID);
        this.partial = false;
    }
}