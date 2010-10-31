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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import se.fnord.rt.client.RTAPIFactory;

public class RepositoryConfiguration implements Serializable {
    private static final long serialVersionUID = 6754960529464167118L;
    private final String userName;
    private final String repositoryURL;
    private final Map<String, QueueInfo> queueInfoByName;
    private final StandardFields standardFields;

    public RepositoryConfiguration(final String repositoryURL, final String userName, final Collection<QueueInfo> queueInfo, final StandardFields standardFields) {
        this.userName = userName;
        this.repositoryURL = repositoryURL;
        this.standardFields = standardFields;

        final Map<String, QueueInfo> infos = new HashMap<String, QueueInfo>(queueInfo.size());
        for (final QueueInfo qi : queueInfo)
            infos.put(qi.getName(), qi);
        queueInfoByName = Collections.unmodifiableMap(infos);
    }

    public String getRepositoryKey() {
        return RTAPIFactory.createRepositoryKey(repositoryURL, userName);
    }

    public QueueInfo getQueueInfo(final String queue) {
        if (!queueInfoByName.containsKey(queue))
            throw new RuntimeException("No such queue configured: " + queue);
        return queueInfoByName.get(queue);
    }

    public StandardFields getStandardFields() {
        return standardFields;
    }

}
