package se.fnord.rt.client.internal.operations;

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.HttpException;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.RTTicket;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.RTObjectFactory;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class GetTicketsFromQuery implements RTOperation<List<RTTicket>> {

    private final String query;

    public GetTicketsFromQuery(final String query) {
        this.query = query;
    }

    @Override
    public List<RTTicket> execute(final URLFactory urls, final RTRequests client) throws HttpException, IOException, RTException, InterruptedException {
        final String url = urls.getAPITicketSearchUrl(query);
        final String result = client.await(client.get(url));

        return RTObjectFactory.createPartialTickets(result);
    }

}
