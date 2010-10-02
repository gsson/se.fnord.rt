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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RTTicket {
    public final int taskId;
    public final Map<RTTicketAttributes, Object> mappedFields;
    public final Map<String, String> unmappedFields;
    public final List<RTHistory> comments;
    public final Map<RTLinkType, List<Integer>> links;
    public final boolean partial;

    public RTTicket(final Map<RTTicketAttributes, Object> mappedFields, final Map<String, String> unmappedFields) {
        this.unmappedFields = new HashMap<String, String>(unmappedFields);
        this.mappedFields = new EnumMap<RTTicketAttributes, Object>(RTTicketAttributes.class);
        this.mappedFields.putAll(mappedFields);
        this.comments = null;
        this.links = null;
        this.taskId = (Integer) mappedFields.get(RTTicketAttributes.ID);
        this.partial = true;
    }

    public RTTicket(final Map<RTTicketAttributes, Object> mappedFields, final Map<String, String> unmappedFields, List<RTHistory> comments, final Map<RTLinkType, List<Integer>> links) {
        this.unmappedFields = new HashMap<String, String>(unmappedFields);
        this.mappedFields = new EnumMap<RTTicketAttributes, Object>(RTTicketAttributes.class);
        this.mappedFields.putAll(mappedFields);
        this.comments = new ArrayList<RTHistory>(comments);
        this.links = new EnumMap<RTLinkType, List<Integer>>(links);
        this.taskId = (Integer) mappedFields.get(RTTicketAttributes.ID);
        this.partial = false;
    }

}
