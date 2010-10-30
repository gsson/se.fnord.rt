package se.fnord.rt.core.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.xml.sax.SAXException;

import se.fnord.rt.client.RTAPI;
import se.fnord.rt.client.RTAuthenticationException;
import se.fnord.rt.client.RTCustomField;
import se.fnord.rt.client.RTQueue;
import se.fnord.rt.core.RequestTrackerCorePlugin;
import se.fnord.rt.core.RequestTrackerRepositoryConnector;

public class RepositoryConfigurationFetcherImpl implements RepositoryConfigurationFetcher {
    private RTQueue getQueue(final TaskRepository repository, final String queueId) throws CoreException {
        try {
            final RTAPI client = RequestTrackerCorePlugin.getDefault().getClient(repository);
            return client.getQueue(queueId);

        } catch (RTAuthenticationException e) {
            throw new CoreException(RepositoryStatus.createLoginError(repository.getRepositoryUrl(), RequestTrackerCorePlugin.PLUGIN_ID));
        } catch (IOException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        } catch (InterruptedException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        }
    }

    private String getVersion(final TaskRepository repository) throws CoreException {
        try {
            final RTAPI client = RequestTrackerCorePlugin.getDefault().getClient(repository);
            return client.getVersion();

        } catch (RTAuthenticationException e) {
            throw new CoreException(RepositoryStatus.createLoginError(repository.getRepositoryUrl(), RequestTrackerCorePlugin.PLUGIN_ID));
        } catch (IOException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        } catch (InterruptedException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        }
    }

    private QueueInfo translateQueueInfo(final RTQueue rtQueue) {
        final List<CustomField> fields = new ArrayList<CustomField>(rtQueue.getTicketFields().size());

        for (final RTCustomField rtField : rtQueue.getTicketFields())
            fields.add(new CustomField(rtField.getName(), rtField.getLabel(), rtField.getDescription(), "string"));

        return new QueueInfo(rtQueue.getId(), rtQueue.getName(), rtQueue.getDescription(), fields);
    }

    private StandardFields getStandardFields(final String version) throws CoreException {
        try {
            final StandardFields standardFields = new StandardFields(version);
            standardFields.load();
            return standardFields;
        } catch (FileNotFoundException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        } catch (IOException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        }
    }

    @Override
    public RepositoryConfiguration fetch(final TaskRepository repository, IProgressMonitor monitor) throws CoreException {
        try {
            final List<String> queueIds = new ArrayList<String>();
            for (Entry<String, String> property : repository.getProperties().entrySet()) {
                if (property.getKey().startsWith(RequestTrackerRepositoryConnector.REPOSITORY_PROPERTY_QUEUE_ID_PREFIX)) {
                    queueIds.add(property.getValue());
                }
            }

            monitor.beginTask("Fetching queue information", queueIds.size() + 1);

            final List<QueueInfo> queues = new ArrayList<QueueInfo>(queueIds.size());
            for (String queueId : queueIds) {
                queues.add(translateQueueInfo(getQueue(repository, queueId)));
                monitor.worked(1);
            }

            final StandardFields standardFields = getStandardFields(getVersion(repository));
            monitor.worked(1);
            return new RepositoryConfiguration(repository.getRepositoryUrl(), repository.getUserName(), queues, standardFields);
        }
        finally {
            monitor.done();
        }
    }
}
