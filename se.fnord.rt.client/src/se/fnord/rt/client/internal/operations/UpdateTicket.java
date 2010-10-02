package se.fnord.rt.client.internal.operations;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.ParseUtils;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class UpdateTicket implements RTOperation<Void> {

    private final String ticketId;
    private final Map<String, String> changed;

    public UpdateTicket(final String ticketId, final Map<String, String> changed) {
        this.ticketId = ticketId;
        this.changed = changed;
    }

    @Override
    public Void execute(final URLFactory urls, final RTRequests client) throws HttpException, IOException, RTException, InterruptedException {
        final String content = ParseUtils.generateAttributes(changed);
        Future<String> post = client.post(urls.getAPITicketUpdateUrl(ticketId), new NameValuePair("content", content));

        client.await(post);

        return null;
    }

}
