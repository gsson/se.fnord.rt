package se.fnord.rt.client.internal.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpException;

import se.fnord.rt.client.RTAuthenticationException;
import se.fnord.rt.client.RTException;
import se.fnord.rt.client.RTTicket;
import se.fnord.rt.client.RTTicketCollector;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.RTObjectFactory;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class GetTicketsFromIds implements RTOperation<List<RTTicket>> {

    private static final class TicketInfoHolder {
        private final RTRequests client;

        private final Future<String> ticketInfo;
        private final Future<String> historyInfo;
        private final Future<String> linkInfo;

        public TicketInfoHolder(final RTRequests client, final Future<String> ticketInfo, final Future<String> historyInfo, final Future<String> linkInfo) {
            this.client = client;
            this.ticketInfo = ticketInfo;
            this.historyInfo = historyInfo;
            this.linkInfo = linkInfo;
        }

        @SuppressWarnings("unchecked")
        public String[] await() throws RTException, HttpException, IOException, InterruptedException {
            return client.awaitMany(ticketInfo, historyInfo, linkInfo);
        }
    }

    private final String[] ticketIds;
    private final RTTicketCollector collector;

    public GetTicketsFromIds(final String... ticketIds) {
        this.ticketIds = ticketIds;
        this.collector = null;
    }

    public GetTicketsFromIds(RTTicketCollector collector, final String... ticketIds) {
        this.collector = collector;
        this.ticketIds = ticketIds;
    }

    @Override
    public List<RTTicket> execute(final URLFactory urls, final RTRequests client) throws HttpException, IOException, RTException, InterruptedException {
        final List<RTTicket> tickets = new ArrayList<RTTicket>(ticketIds.length);
        final List<TicketInfoHolder> holders = new ArrayList<TicketInfoHolder>(ticketIds.length);
        for (String ticketId : ticketIds) {
            holders.add(new TicketInfoHolder(client,
                client.get(urls.getAPITicketUrl(ticketId)),
                client.get(urls.getAPITicketHistoryUrl(ticketId)),
                client.get(urls.getAPITicketLinksUrl(ticketId))
            ));
        }

        for (TicketInfoHolder holder : holders) {
            try {
                final String[] result = holder.await();
                final RTTicket ticket = RTObjectFactory.createFullTicket(result[0], result[1], result[2]);
                try {
                    if (collector != null)
                        collector.accept(ticket);
                }
                catch (IOException e) {
                    throw e;
                }
                catch (InterruptedException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                tickets.add(ticket);
            }
            catch (RTAuthenticationException e) {
                throw e;
            }
            catch (RTException rte) {}
        }

        return tickets;
    }
}
