package se.fnord.rt.client.internal.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
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
