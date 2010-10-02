package se.fnord.rt.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;

import se.fnord.rt.client.internal.RTExecutor;
import se.fnord.rt.client.internal.operations.AddComment;
import se.fnord.rt.client.internal.operations.GetQueue;
import se.fnord.rt.client.internal.operations.GetTicket;
import se.fnord.rt.client.internal.operations.GetTicketsFromIds;
import se.fnord.rt.client.internal.operations.GetTicketsFromQuery;
import se.fnord.rt.client.internal.operations.GetUser;
import se.fnord.rt.client.internal.operations.UpdateLinks;
import se.fnord.rt.client.internal.operations.UpdateTicket;

public class RTAPI {
    private final RTExecutor executor;

    public RTAPI(final RTExecutor executor) {
        this.executor = executor;
    }

    public RTTicket getTicket(final String ticketId) throws RTException, HttpException, IOException, InterruptedException {
        return executor.execute(new GetTicket(ticketId));
    }

    public List<RTTicket> getTicketsFromIds(final RTTicketCollector collector, final String...ticketIds) throws RTException, HttpException, IOException, InterruptedException {
        return executor.execute(new GetTicketsFromIds(collector, ticketIds));
    }

    public List<RTTicket> getTicketsFromIds(final String...ticketIds) throws RTException, HttpException, IOException, InterruptedException {
        return executor.execute(new GetTicketsFromIds(ticketIds));
    }

    public List<RTTicket> getTicketsFromQuery(final String query) throws RTException, HttpException, IOException, InterruptedException {
        return executor.execute(new GetTicketsFromQuery(query));
    }

    public RTUser getUser(final String userId) throws HttpException, IOException, RTException, InterruptedException {
        return executor.execute(new GetUser(userId));
    }

    public RTQueue getQueue(final String queueId) throws RTException, HttpException, IOException, InterruptedException {
        return executor.execute(new GetQueue(queueId));
    }

    public void updateTicket(final String ticketId, final Map<String, String> changed) throws HttpException, IOException, RTException, InterruptedException {
        executor.execute(new UpdateTicket(ticketId, changed));
    }

    public void addComment(final String ticketId, final String comment) throws HttpException, IOException, RTException, InterruptedException {
        executor.execute(new AddComment(ticketId, comment));
    }

    public void updateLinks(final String ticketId, final Map<String, String> links) throws RTException, HttpException, IOException, InterruptedException {
        executor.execute(new UpdateLinks(ticketId, links));
    }
}
