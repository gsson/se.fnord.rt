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
package se.fnord.rt.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

import se.fnord.rt.client.RTAPI;
import se.fnord.rt.client.RTAuthenticationException;
import se.fnord.rt.client.RTLinkType;
import se.fnord.rt.client.RTTicket;
import se.fnord.rt.client.RTTicketAttributes;
import se.fnord.rt.client.RTTicketCollector;
import se.fnord.rt.core.internal.RepositoryConfiguration;
import se.fnord.rt.core.internal.TaskDataBuilder;

public class RequestTrackerTaskDataHandler extends AbstractTaskDataHandler {
    public static final String QUERY_ID = "rt.query";

    public RequestTrackerTaskDataHandler() {
        super();
    }

    private static final class TicketCollector implements RTTicketCollector {
        private final TaskDataCollector collector;
        private final TaskDataBuilder taskDataBuilder;
        private final IProgressMonitor monitor;

        public TicketCollector(final TaskDataBuilder taskDataBuilder, final TaskDataCollector collector, final IProgressMonitor monitor) {
            this.taskDataBuilder = taskDataBuilder;
            this.collector = collector;
            this.monitor = monitor;
        }
        @Override
        public void accept(RTTicket ticket) {
            this.collector.accept(taskDataBuilder.createTaskData(ticket));
            monitor.worked(1);
        }
    }

    @Override
    public void getMultiTaskData(TaskRepository repository, Set<String> taskIds, TaskDataCollector collector,
            IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Fetching tickets", taskIds.size() + 1);
        try {
            final RepositoryConfiguration repositoryConfiguration = RequestTrackerCorePlugin.getDefault().getConfigurationCache().getConfiguration(repository, new SubProgressMonitor(monitor, 4));
            monitor.worked(1);

            final RTAPI client = RequestTrackerCorePlugin.getDefault().getClient(repository);
            final TaskDataBuilder taskDataBuilder = new TaskDataBuilder(repository, repositoryConfiguration, getAttributeMapper(repository));

            client.getTicketsFromIds(new TicketCollector(taskDataBuilder, collector, monitor), taskIds.toArray(new String[taskIds.size()]));

        } catch (RTAuthenticationException e) {
            throw new CoreException(RepositoryStatus.createLoginError(repository.getRepositoryUrl(), RequestTrackerCorePlugin.PLUGIN_ID));
        } catch (IOException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        } catch (InterruptedException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        }
        finally {
            monitor.done();
        }
    }

    @Override
    public boolean canGetMultiTaskData(TaskRepository taskRepository) {
        return true;
    }

    @Override
    public TaskAttributeMapper getAttributeMapper(TaskRepository repository) {
        return new TaskAttributeMapper(repository);
    }

    @Override
    public boolean initializeTaskData(TaskRepository repository, TaskData data, ITaskMapping initializationData, IProgressMonitor monitor)
            throws CoreException {
        return false;
    }

    @Override
    public RepositoryResponse postTaskData(TaskRepository repository, TaskData data, Set<TaskAttribute> oldAttributes, IProgressMonitor monitor)
            throws CoreException {
        try {
            if (data.isNew())
                return null;

            final String taskId = data.getTaskId();

            monitor.beginTask("Updating task #"+ data.getTaskId(), 1);

            final TaskAttribute root = data.getRoot();
            final TaskAttributeMapper mapper = data.getAttributeMapper();
            String comment = null;
            final Map<String,String> stringAttributes = new HashMap<String, String>();
            final HashMap<String, String> links = new HashMap<String, String>();

            for (final TaskAttribute oldAttribute : oldAttributes) {
                final String attributeId = oldAttribute.getId();
                final TaskAttribute attribute = root.getAttribute(attributeId);

                if (attributeId.startsWith(TaskAttribute.PREFIX_COMMENT))
                    return null;

                if (TaskAttribute.COMMENT_NEW.equals(attributeId)) {
                    comment = attribute.getValue();
                    continue;
                }

                if (attributeId.startsWith(RTTicketAttributes.RT_ATTRIBUTE_PREFIX)) {
                    stringAttributes.put(attributeId.substring(RTTicketAttributes.RT_ATTRIBUTE_PREFIX.length()), attribute.getValue());
                }
                else if (attributeId.startsWith(RTLinkType.RT_LINK_PREFIX)) {
                    final RTLinkType linkType = RTLinkType.getById(attributeId);
                    if (linkType == null)
                        return null;
                    links.put(linkType.getName(), linkType.dump(linkType.createObject(mapper, attribute)));
                }
                else {
                    final RTTicketAttributes type = RTTicketAttributes.getById(attributeId);
                    if (type == null)
                        return null;
                    stringAttributes.put(type.getName(), type.dump(type.createObject(mapper, attribute)));
                }
            }

            final RTAPI client = RequestTrackerCorePlugin.getDefault().getClient(repository);

            try {
                if (!stringAttributes.isEmpty())
                    client.updateTicket(taskId, stringAttributes);
                if (!links.isEmpty())
                    client.updateLinks(taskId, links);
                if (comment != null)
                    client.addComment(taskId, comment);
            } catch (RTAuthenticationException e) {
                throw new CoreException(RepositoryStatus.createLoginError(repository.getRepositoryUrl(), RequestTrackerCorePlugin.PLUGIN_ID));
            } catch (IOException e) {
                throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
            } catch (InterruptedException e) {
                throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
            }

            return new RepositoryResponse(ResponseKind.TASK_UPDATED, taskId);
        }
        finally {
            monitor.done();
        }
    }

    public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask("Fetching task #" + taskId, 2);
            final RepositoryConfiguration repositoryConfiguration = RequestTrackerCorePlugin.getDefault().getConfigurationCache().getConfiguration(repository, new SubProgressMonitor(monitor, 4));
            monitor.worked(1);
            final RTAPI client = RequestTrackerCorePlugin.getDefault().getClient(repository);
            final RTTicket task = client.getTicket(taskId);
            monitor.worked(1);

            return new TaskDataBuilder(repository, repositoryConfiguration, getAttributeMapper(repository)).createTaskData(task);
        } catch (RTAuthenticationException e) {
            throw new CoreException(RepositoryStatus.createLoginError(repository.getRepositoryUrl(), RequestTrackerCorePlugin.PLUGIN_ID));
        } catch (IOException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        } catch (InterruptedException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        }
        finally {
            monitor.done();
        }
    }

    public void performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector collector,
            IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask("Performing query", 5);
            final RepositoryConfiguration repositoryConfiguration = RequestTrackerCorePlugin.getDefault().getConfigurationCache().getConfiguration(repository, new SubProgressMonitor(monitor, 4));

            final String queryString = query.getAttribute(QUERY_ID);
            final RTAPI client = RequestTrackerCorePlugin.getDefault().getClient(repository);
            final List<RTTicket> tasks = client.getTicketsFromQuery(queryString);
            final TaskDataBuilder taskDataBuilder = new TaskDataBuilder(repository, repositoryConfiguration, getAttributeMapper(repository));

            for (final RTTicket task : tasks)
                collector.accept(taskDataBuilder.createTaskData(task));

        } catch (RTAuthenticationException e) {
            throw new CoreException(RepositoryStatus.createLoginError(repository.getRepositoryUrl(), RequestTrackerCorePlugin.PLUGIN_ID));
        } catch (IOException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        } catch (InterruptedException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, e.getMessage(), e));
        }
        finally {
            monitor.done();
        }
    }

}
