package se.fnord.rt.client.internal.operations;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.RTUser;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.client.internal.RTObjectFactory;
import se.fnord.rt.client.internal.RTOperation;
import se.fnord.rt.client.internal.RTRequests;

public class GetUser implements RTOperation<RTUser> {

    private final String userId;
    public GetUser(final String userId) {
        this.userId = userId;
    }
    @Override
    public RTUser execute(URLFactory urls, RTRequests client) throws HttpException, IOException, RTException, InterruptedException {
        return RTObjectFactory.createUser(client.await(client.get(urls.getAPIUserUrl(userId))));
    }

}
