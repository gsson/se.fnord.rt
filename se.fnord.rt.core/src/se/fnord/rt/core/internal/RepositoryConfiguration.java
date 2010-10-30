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
