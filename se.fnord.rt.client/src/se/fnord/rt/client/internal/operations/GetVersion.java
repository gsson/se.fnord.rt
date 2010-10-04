package se.fnord.rt.client.internal.operations;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class GetVersion implements RTOperation<String> {

    public GetVersion() {
    }

    @Override
    public String execute(final URLFactory urls, final RTRequests client) throws HttpException, IOException, RTException, InterruptedException {
        return client.getVersion();
    }

}
