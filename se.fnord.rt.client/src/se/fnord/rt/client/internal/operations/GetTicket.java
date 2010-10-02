package se.fnord.rt.client.internal.operations;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.RTTicket;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.RTObjectFactory;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class GetTicket implements RTOperation<RTTicket> {

    private final String ticketId;

    public GetTicket(final String ticketId) {
        this.ticketId = ticketId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RTTicket execute(final URLFactory urls, final RTRequests client) throws HttpException, IOException, RTException, InterruptedException {
        final String[] result = client.awaitMany(
            client.get(urls.getAPITicketUrl(ticketId)),
            client.get(urls.getAPITicketHistoryUrl(ticketId)),
            client.get(urls.getAPITicketLinksUrl(ticketId))
        );

        return RTObjectFactory.createFullTicket(result[0], result[1], result[2]);

    }

}
