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
