package se.fnord.rt.client.internal.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.ParseUtils;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class AddComment implements RTOperation<Void> {

    private final String ticketId;
    private final String comment;

    public AddComment(final String ticketId, final String comment) {
        this.ticketId = ticketId;
        this.comment = comment;
    }

    @Override
    public Void execute(final URLFactory urls, final RTRequests client) throws HttpException, IOException, RTException, InterruptedException {
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("Action", "comment");
        attributes.put("Text", comment);
        final String content = ParseUtils.generateAttributes(attributes);
        final Future<String> post = client.post(urls.getAPITicketNewCommentUrl(ticketId), new NameValuePair("content", content));

        client.await(post);

        return null;
    }

}
