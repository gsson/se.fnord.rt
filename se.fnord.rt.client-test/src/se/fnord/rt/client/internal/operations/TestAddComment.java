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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.RTRequests;
import se.fnord.rt.client.testutil.ConstantValueFuture;

public class TestAddComment {
        private URLFactory urls;
        private IMocksControl ctrl;

        @Before
        public void setUp() throws MalformedURLException {
            urls = URLFactory.create("http://localhost/");
            ctrl = EasyMock.createStrictControl();
        }

        @Test
        public void addComment() throws RTException, HttpException, IOException, InterruptedException, ExecutionException {
            final RTRequests req = ctrl.createMock(RTRequests.class);
            final Future<String> v = new ConstantValueFuture<String>("");

            expect(req.post(
                urls.getAPITicketNewCommentUrl("14"),
                new NameValuePair("content", "Text: Hello World\nAction: comment\n"))
            ).andReturn(v);
            expect(req.await(v)).andReturn(v.get());

            ctrl.replay();

            AddComment op = new AddComment("14", "Hello World");
            op.execute(urls, req);
        }

        @Test
        public void addMultilineComment() throws RTException, HttpException, IOException, InterruptedException, ExecutionException {
            final RTRequests req = ctrl.createMock(RTRequests.class);
            final Future<String> v = new ConstantValueFuture<String>("");

            expect(req.post(
                urls.getAPITicketNewCommentUrl("14"),
                new NameValuePair("content", "Text: Hello\n      World\nAction: comment\n"))
            ).andReturn(v);
            expect(req.await(v)).andReturn(v.get());

            ctrl.replay();

            AddComment op = new AddComment("14", "Hello\nWorld");
            op.execute(urls, req);
        }

}
