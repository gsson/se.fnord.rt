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
package se.fnord.rt.client.internal.operations;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.ParseUtils;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class UpdateLinks implements RTOperation<Void> {

    private final String ticketId;
    private final Map<String, String> links;

    public UpdateLinks(final String ticketId, final Map<String, String> links) {
        this.ticketId = ticketId;
        this.links = links;
    }

    @Override
    public Void execute(final URLFactory urls, final RTRequests client) throws HttpException, IOException, RTException, InterruptedException {
        final String content = ParseUtils.generateAttributes(links);
        client.await(client.post(urls.getAPITicketLinksUpdateUrl(ticketId), new NameValuePair("content", content)));

        return null;
    }

}
