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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RTObjectFactory {
    private RTObjectFactory() {
    }

    public static RTUser createUser(final String data) {
        final Map<String, String> attributes = new HashMap<String, String>();

        ParseUtils.parseAttributes(data, attributes);
        ParseUtils.filterNotSet(attributes);

        EnumMap<RTUserAttributes, Object> fields = new EnumMap<RTUserAttributes, Object>(RTUserAttributes.class);
        for (RTUserAttributes attr : RTUserAttributes.values())
            if (attributes.containsKey(attr.getName()))
                fields.put(attr, attr.parse(attributes.get(attr.getName())));

        return new RTUser(fields);
    }

    public static RTTicket createPartialTicket(final String data) {
        final Map<String, String> attributes = new HashMap<String, String>();

        ParseUtils.parseAttributes(data, attributes);
        ParseUtils.filterNotSet(attributes);

        final EnumMap<RTTicketAttributes, Object> fields = new EnumMap<RTTicketAttributes, Object>(RTTicketAttributes.class);
        final Iterator<Map.Entry<String, String>> i = attributes.entrySet().iterator();
        while (i.hasNext()) {
            final Map.Entry<String, String> e = i.next();
            final RTTicketAttributes attr = RTTicketAttributes.getByName(e.getKey());
            if (attr != null) {
                i.remove();
                fields.put(attr, attr.parse(e.getValue()));
            }
        }

        return new RTTicket(fields, attributes);
    }

    public static RTTicket createFullTicket(final String data, final String multiPartHistory) {
        final Map<String, String> attributes = new HashMap<String, String>();

        ParseUtils.parseAttributes(data, attributes);
        ParseUtils.filterNotSet(attributes);

        final EnumMap<RTTicketAttributes, Object> fields = new EnumMap<RTTicketAttributes, Object>(RTTicketAttributes.class);
        final Iterator<Map.Entry<String, String>> i = attributes.entrySet().iterator();
        while (i.hasNext()) {
            final Map.Entry<String, String> e = i.next();
            final RTTicketAttributes attr = RTTicketAttributes.getByName(e.getKey());
            if (attr != null) {
                i.remove();
                fields.put(attr, attr.parse(e.getValue()));
            }
        }

        List<RTHistory> history = createHistory(multiPartHistory);

        return new RTTicket(fields, attributes, history);
    }

    public static List<RTTicket> createPartialTickets(final String multiPartTickets) {
        final String[] ticketParts = ParseUtils.splitMultiPart(multiPartTickets);
        final ArrayList<RTTicket> tickets = new ArrayList<RTTicket>(ticketParts.length);
        for (final String ticketPart : ticketParts) {
            tickets.add(createPartialTicket(ticketPart));
        }
        return tickets;
    }

    private static List<RTHistory> createHistory(final String multiPartHistory) {
        final String[] historyParts = ParseUtils.splitMultiPart(multiPartHistory);
        final ArrayList<RTHistory> history = new ArrayList<RTHistory>(historyParts.length);
        for (String historyPart : historyParts) {
            history.add(createHistoryItem(historyPart));
        }
        return history;
    }

    private static RTHistory createHistoryItem(final String data) {
        final Map<String, String> attributes = new HashMap<String, String>();
        ParseUtils.parseAttributes(data, attributes);
        ParseUtils.filterNotSet(attributes);

        EnumMap<RTHistoryAttributes, Object> fields = new EnumMap<RTHistoryAttributes, Object>(RTHistoryAttributes.class);
        for (RTHistoryAttributes attr : RTHistoryAttributes.values())
            if (attributes.containsKey(attr.getName()))
                fields.put(attr, attr.parse(attributes.get(attr.getName())));

        return new RTHistory(fields);
    }

}
