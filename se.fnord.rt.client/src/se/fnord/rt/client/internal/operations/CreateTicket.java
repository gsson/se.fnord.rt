package se.fnord.rt.client.internal.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.ParseUtils;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class CreateTicket implements RTOperation<String> {
    private static final Pattern CREATED = Pattern.compile("# Ticket (\\d+) created");
    private final Map<String, String> attributes;

    public CreateTicket(final Map<String, String> attributes) {
        this.attributes = new HashMap<String, String>(attributes);
        this.attributes.put("id", "ticket/new");
    }

    @Override
    public String execute(URLFactory urls, RTRequests client) throws HttpException, IOException, RTException,
            InterruptedException {
        final String content = ParseUtils.generateAttributes(attributes);
        Future<String> post = client.post(urls.getAPITicketNewUrl(), new NameValuePair("content", content));

        String await = client.await(post);
        Matcher matcher = CREATED.matcher(await);
        if (!matcher.find())
            throw new RuntimeException("Could not parse response");

        return matcher.group(1);
    }

}
