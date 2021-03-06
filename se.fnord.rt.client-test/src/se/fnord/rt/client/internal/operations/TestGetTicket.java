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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpException;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.RTTicket;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.RTRequests;
import se.fnord.rt.client.testutil.ConstantValueFuture;
import se.fnord.rt.client.testutil.Utils;

public class TestGetTicket {
        private URLFactory urls;
        private IMocksControl ctrl;

        @Before
        public void setUp() throws MalformedURLException {
            urls = URLFactory.create("http://localhost/");
            ctrl = EasyMock.createStrictControl();
        }


        @SuppressWarnings("unchecked")
        @Test
        public void getTicket() throws RTException, HttpException, IOException, InterruptedException, ExecutionException {
            final RTRequests req = ctrl.createMock(RTRequests.class);

            final Future<String> ticketInfo = new ConstantValueFuture<String>(Utils.getFile(getClass().getResource("ticket14.txt")));
            final Future<String> linksInfo = new ConstantValueFuture<String>(Utils.getFile(getClass().getResource("ticket14_links.txt")));
            final Future<String> historyInfo = new ConstantValueFuture<String>(Utils.getFile(getClass().getResource("ticket14_history.txt")));

            expect(req.get(urls.getAPITicketUrl("14"))).andReturn(ticketInfo);
            expect(req.get(urls.getAPITicketHistoryUrl("14"))).andReturn(historyInfo);
            expect(req.get(urls.getAPITicketLinksUrl("14"))).andReturn(linksInfo);

            expect(req.awaitMany(ticketInfo, historyInfo, linksInfo)).andReturn(new String[] { ticketInfo.get(), historyInfo.get(), linksInfo.get() });

            ctrl.replay();

            GetTicket op = new GetTicket("14");
            RTTicket ticket = op.execute(urls, req);
            assertEquals(6, ticket.comments.size());
            assertFalse(ticket.partial);
            assertEquals(14, ticket.ticketId);
            assertEquals("gsson", ticket.fields.get("Creator"));
        }

}
