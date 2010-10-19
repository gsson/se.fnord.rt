package se.fnord.rt.client;

public interface RTTicketCollector {
    void accept(RTTicket ticket) throws Exception;
}
