package se.fnord.rt.client.internal;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import se.fnord.rt.client.RTException;
import se.fnord.rt.client.URLFactory;

public interface RTOperation<T> {

    T execute(URLFactory urls, RTRequests client) throws HttpException, IOException, RTException, InterruptedException;
}
