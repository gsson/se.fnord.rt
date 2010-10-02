/*
 * Copyright (c) 2010 Henrik Gustafsson <henrik.gustafsson@fnord.se>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package se.fnord.rt.client;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import se.fnord.rt.client.internal.RTClient;

public class RTAPIFactory {
    private static final MultiThreadedHttpConnectionManager CONNECTION_MANAGER = new MultiThreadedHttpConnectionManager();
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(16));

    private final Map<String, RTClient> clients = new HashMap<String, RTClient>();

    private static String makeKey(final String repositoryUrl, final String username, final String password) {
        try {
            if (username == null || password == null)
                return repositoryUrl;

            URI uri = URI.create(repositoryUrl);
            return uri.getScheme() + URIUtil.encodeWithinAuthority(username) + ":" + URIUtil.encodeWithinAuthority(password) + "@" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
        } catch (URIException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized RTAPI getClient(final String repositoryUrl, final String username, final String password) {
        final String key = makeKey(repositoryUrl, username, password);
        RTClient client = clients.get(key);
        if (client == null) {
            client = new RTClient(CONNECTION_MANAGER, EXECUTOR, repositoryUrl, username, password);
            clients.put(key, client);
        }
        return new RTAPI(client);
    }
}
