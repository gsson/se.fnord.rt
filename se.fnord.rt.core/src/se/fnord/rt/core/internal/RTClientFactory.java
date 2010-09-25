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

package se.fnord.rt.core.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public class RTClientFactory {
    
    private final Map<String, RTClient> clients = new HashMap<String, RTClient>();
    
    private static String makeKey(TaskRepository repository) {
        try {
            AuthenticationCredentials credentials = repository.getCredentials(AuthenticationType.HTTP);
            if (credentials == null)
                return repository.getRepositoryUrl();

            URI uri = URI.create(repository.getRepositoryUrl());
            return uri.getScheme() + URIUtil.encodeWithinAuthority(credentials.getUserName()) + ":" + URIUtil.encodeWithinAuthority(credentials.getPassword()) + "@" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
        } catch (URIException e) {
            throw new RuntimeException(e);
        }
    }
    
    public synchronized RTClient getClient(TaskRepository repository) {
        String key = makeKey(repository);
        RTClient client = clients.get(key);
        if (client == null) {
            client = new RTClient(repository);
            clients.put(key, client);
        }
        return client;
    }
}
