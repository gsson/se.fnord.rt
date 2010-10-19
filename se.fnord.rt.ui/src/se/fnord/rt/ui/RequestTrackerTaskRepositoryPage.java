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
package se.fnord.rt.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.swt.widgets.Composite;

import se.fnord.rt.client.RTAPI;
import se.fnord.rt.client.RTAPIFactory;
import se.fnord.rt.client.RTAuthenticationException;
import se.fnord.rt.client.RTException;
import se.fnord.rt.client.RTQueue;
import se.fnord.rt.core.RequestTrackerRepositoryConnector;

@SuppressWarnings("restriction")
public class RequestTrackerTaskRepositoryPage extends AbstractRepositorySettingsPage implements ITaskRepositoryPage {
    private static final String MSG_SETTINGS_TITLE = "Request Tracker Repository Settings";
    private QueueWidget queueWidget;

    public RequestTrackerTaskRepositoryPage(TaskRepository repository) {
        super(MSG_SETTINGS_TITLE, "", repository);
        setNeedsValidation(true);
        setNeedsAnonymousLogin(false);
        setNeedsAdvanced(true);
        setNeedsProxy(false);
        setNeedsEncoding(false);
    }

    @Override
    public String getConnectorKind() {
        return RequestTrackerRepositoryConnector.REPOSITORY_CONNECTOR_KIND;
    }

    private void pruneQueueProperties(TaskRepository repository) {
        for (final Map.Entry<String, String> e: repository.getProperties().entrySet())
            if (e.getKey().startsWith(RequestTrackerRepositoryConnector.REPOSITORY_PROPERTY_QUEUE_ID_PREFIX))
                repository.removeProperty(e.getKey());
    }

    private void createQueueProperty(TaskRepository repository, int n, Queue queue) {
        repository.setProperty(RequestTrackerRepositoryConnector.REPOSITORY_PROPERTY_QUEUE_ID_PREFIX + n, queue.getId().toString());
    }

    private List<Integer> getQueueIds(TaskRepository repository) {
        final List<Integer> ids = new ArrayList<Integer>();
        for (final Map.Entry<String, String> e: repository.getProperties().entrySet()) {
            if (e.getKey().startsWith(RequestTrackerRepositoryConnector.REPOSITORY_PROPERTY_QUEUE_ID_PREFIX)) {
                try {
                ids.add(Integer.parseInt(e.getValue()));
                }
                catch (NumberFormatException ex) {

                }
            }
        }
        return ids;
    }

    @Override
    public void applyTo(TaskRepository repository) {
        super.applyTo(repository);
        try {
            final AuthenticationCredentials credentials = repository.getCredentials(AuthenticationType.REPOSITORY);
            final RTAPI rtClient = new RTAPIFactory().getClient(repository.getRepositoryUrl(), credentials.getUserName(), credentials.getPassword());

            validateQueues(rtClient, false);

            pruneQueueProperties(repository);

            int i = 0;
            for (Queue queue : queueWidget.getQueues())
                createQueueProperty(repository, i++, queue);

            repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY, IRepositoryConstants.CATEGORY_BUGS);
        } catch (RTAuthenticationException e) {
            /* TODO: Handle exceptions */
            throw new RuntimeException(e);
        } catch (Exception e) {
            /* TODO: Handle exceptions */
            throw new RuntimeException(e);
        }
    }

    public boolean checkServerURI(String server) {
        if (server == null || server.length() == 0) {
            setErrorMessage("Enter a server address.");
            return false;
        }
        try {
            URL url = new URL(server);
            url.toURI();
        } catch (MalformedURLException e) {
            setErrorMessage("The provided server address is not a valid Request Tracker URI.");
            return false;
        } catch (URISyntaxException e) {
            setErrorMessage("The provided server address is not a valid Request Tracker URI.");
            return false;
        }
        setErrorMessage(null);
        return true;
    }

    @Override
    protected void createAdditionalControls(Composite parent) {
        queueWidget = new QueueWidget(parent, 0);
        try {
            final AuthenticationCredentials credentials = repository.getCredentials(AuthenticationType.REPOSITORY);
            if (credentials == null)
                return;
            if (repository.getRepositoryUrl() == null)
                return;

            final RTAPI rtClient = new RTAPIFactory().getClient(repository.getRepositoryUrl(), credentials.getUserName(), credentials.getPassword());
            final List<Queue> queues = queueWidget.getQueues();

            queues.clear();
            for (final int i : getQueueIds(getRepository())) {
                final Queue queue = new Queue();
                queue.setId(i);
                queues.add(queue);
            }

            validateQueues(rtClient, true);
        } catch (RTAuthenticationException e) {
        } catch (Exception e) {
        }

    }

    @Override
    protected boolean isValidUrl(String url) {
        return checkServerURI(url);
    }

    private void validateQueues(final RTAPI rtClient, final boolean refresh) throws RTException, HttpException, IOException, InterruptedException {
        final HashSet<Queue> dupeFilter = new HashSet<Queue>();
        final Iterator<Queue> queues = queueWidget.getQueues().iterator();
        while (queues.hasNext()) {
            /* TODO: Remove or mark missing queues */
            final Queue queue = queues.next();
            if (!queue.isVerified() || refresh) {
                final RTQueue rtQueue = rtClient.getQueue((queue.getId() != null)?Integer.toString(queue.getId()):queue.getName());

                queue.setId(rtQueue.getId());
                queue.setName(rtQueue.getName());
                queue.setDescription(rtQueue.getDescription());
                queue.setVerified(true);

                if (dupeFilter.contains(queue))
                    queues.remove();
                else
                    dupeFilter.add(queue);
            }

        }
        queueWidget.refresh();
    }

    @Override
    protected Validator getValidator(final TaskRepository repository) {
        return new Validator() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    final AuthenticationCredentials credentials = repository.getCredentials(AuthenticationType.REPOSITORY);
                    final RTAPI rtClient = new RTAPIFactory().getClient(repository.getRepositoryUrl(), credentials.getUserName(), credentials.getPassword());

                    rtClient.getUser(repository.getUserName());

                    validateQueues(rtClient, true);
                } catch (RTAuthenticationException e) {
                    setStatus(RepositoryStatus.createLoginError(repository.getRepositoryUrl(), RequestTrackerUIPlugin.PLUGIN_ID));
                } catch (Exception e) {
                    setStatus(RepositoryStatus.createStatus(repository, IStatus.ERROR, RequestTrackerUIPlugin.PLUGIN_ID, e.toString()));
                }
            }
        };
    }
}
