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
import java.util.List;

public class QueueInfo implements Serializable {
    private static final long serialVersionUID = 6621528758030183289L;
    private final int id;
    private final String name;
    private final String description;
    private final CustomFields ticketCustomFields;

    public QueueInfo(final int id, final String name, final String description, final List<CustomField> ticketCustomFields) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ticketCustomFields = new CustomFields(ticketCustomFields);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public CustomFields getTicketCustomFields() {
        return ticketCustomFields;
    }
}
