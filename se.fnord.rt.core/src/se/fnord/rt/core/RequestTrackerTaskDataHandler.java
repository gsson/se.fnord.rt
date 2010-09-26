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

import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

import se.fnord.rt.core.internal.RTClient;
import se.fnord.rt.core.internal.RTException;
import se.fnord.rt.core.internal.RTTicket;
import se.fnord.rt.core.internal.RTTicketAttributes;
import se.fnord.rt.core.internal.TaskDataBuilder;

public class RequestTrackerTaskDataHandler extends AbstractTaskDataHandler {
    public static final String QUERY_ID = "rt.query";

    public RequestTrackerTaskDataHandler() {
        super();
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
                else {
                    final RTTicketAttributes type = RTTicketAttributes.getById(attributeId);
                    if (type == null)
                        return null;
                    stringAttributes.put(type.getName(), type.dump(type.createObject(mapper, attribute)));
                }
            }

            final RTClient client = RequestTrackerCorePlugin.getDefault().getClient(repository);

            try {
                if (!stringAttributes.isEmpty())
                    client.updateTask(taskId, stringAttributes);
                if (comment != null)
                    client.addComment(taskId, comment);
            } catch (HttpException e) {
                return null;
            } catch (IOException e) {
                return null;
            } catch (RTException e) {
                return null;
            }

            return new RepositoryResponse(ResponseKind.TASK_UPDATED, taskId);
        }
        finally {
            monitor.done();
        }
    }

    public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor) {
        try {
            monitor.beginTask("Fetching task #" + taskId, 1);
            final RTClient client = RequestTrackerCorePlugin.getDefault().getClient(repository);
            final RTTicket task = client.getTask(taskId);
            monitor.worked(1);

            return new TaskDataBuilder(repository, getAttributeMapper(repository)).createTaskData(task);
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            monitor.done();
        }
        return null;
    }

    public void performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector collector,
            IProgressMonitor monitor) {
        try {
            monitor.beginTask("Performing query", 1);

            final String queryString = query.getAttribute(QUERY_ID);
            final RTClient client = RequestTrackerCorePlugin.getDefault().getClient(repository);
            final List<RTTicket> tasks = client.getQuery(queryString);
            final TaskDataBuilder taskDataBuilder = new TaskDataBuilder(repository, getAttributeMapper(repository));

            for (final RTTicket task : tasks)
                collector.accept(taskDataBuilder.createTaskData(task));

        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            monitor.done();
        }
    }
}
