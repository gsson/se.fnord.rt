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
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.fnord.rt.client.internal.attributes.IdParser;

public class RTTicket {
    private static final String ID = "id";
    private static final String QUEUE = "Queue";

    public final int ticketId;
    public final String queue;
    public final Map<String, String> fields;

    public final List<RTHistory> comments;
    public final Map<RTLinkType, List<Integer>> links;
    public final boolean partial;

    public RTTicket(final Map<String, String> fields) {
        this.fields = Collections.unmodifiableMap(new HashMap<String, String>(fields));
        this.comments = null;
        this.links = null;
        this.ticketId = new IdParser("ticket").parse(fields.get(ID));
        this.queue = fields.get(QUEUE);
        this.partial = true;
    }

    public RTTicket(final Map<String, String> fields, List<RTHistory> comments, final Map<RTLinkType, List<Integer>> links) {
        this.fields = Collections.unmodifiableMap(new HashMap<String, String>(fields));
        this.comments = new ArrayList<RTHistory>(comments);
        this.links = new EnumMap<RTLinkType, List<Integer>>(links);
        this.ticketId = new IdParser("ticket").parse(fields.get(ID));
        this.queue = fields.get(QUEUE);
        this.partial = false;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getQueue() {
        return queue;
    }

    public String getField(final String field) {
        return fields.get(field);
    }

    public List<Integer> getLink(final RTLinkType link) {
        return links.get(link);
    }

    public boolean isPartial() {
        return partial;
    }

    public List<RTHistory> getComments() {
        return comments;
    }
}
