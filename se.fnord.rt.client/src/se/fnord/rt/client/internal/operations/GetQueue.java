package se.fnord.rt.client.internal.operations;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.RTQueue;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.RTObjectFactory;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class GetQueue implements RTOperation<RTQueue> {

    private final String queueId;

    public GetQueue(final String queueId) {
        this.queueId = queueId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RTQueue execute(final URLFactory urls, final RTRequests client) throws HttpException, IOException, RTException, InterruptedException {
        final String[] result = client.awaitMany(
            client.get(urls.getAPIQueueUrl(queueId)),
            client.get(urls.getAPIQueueCustomFieldsUrl(queueId)),
            client.get(urls.getAPIQueueTicketCustomFieldsUrl(queueId))
        );

        return RTObjectFactory.createQueue(result[0], result[1], result[2]);

    }

}
