package se.fnord.rt.client.internal.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

import static org.easymock.EasyMock.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.CharBuffer;
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
import se.fnord.rt.client.RTTicket;
import se.fnord.rt.client.RTTicketAttributes;
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
            assertEquals(14, ticket.mappedFields.get(RTTicketAttributes.ID));
            assertEquals("gsson", ticket.mappedFields.get(RTTicketAttributes.CREATOR));
        }

}
