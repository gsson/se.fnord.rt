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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import se.fnord.rt.client.RTQueue;
import se.fnord.rt.client.URLFactory;

public class RequestTrackerRepositoryConnector extends AbstractRepositoryConnector {
    public static final String REPOSITORY_PROPERTY_QUEUE_ID_PREFIX = "rt.queue.id.";
    public static final String REPOSITORY_CONNECTOR_KIND = "RequestTracker";
    public static final String REPOSITORY_TYPE_LABEL = "Request Tracker";
    public static final String REPOSITORY_TYPE_SHORT_LABEL = "RT";

    private final RequestTrackerTaskDataHandler taskDataHandler;

    public RequestTrackerRepositoryConnector() {
        this.taskDataHandler = new RequestTrackerTaskDataHandler();
    }

    @Override
    public boolean canCreateNewTask(TaskRepository repo) {
        return true;
    }

    @Override
    public boolean canCreateTaskFromKey(TaskRepository repo) {
        return true;
    }

    @Override
    public String getConnectorKind() {
        return REPOSITORY_CONNECTOR_KIND;
    }

    @Override
    public String getLabel() {
        return REPOSITORY_TYPE_LABEL;
    }

    @Override
    public String getShortLabel() {
        return REPOSITORY_TYPE_SHORT_LABEL;
    }

    @Override
    public String getRepositoryUrlFromTaskUrl(String url) {
        if (url == null)
            return null;

        int index = url.lastIndexOf("/ticket/");
        if (index == -1)
            return null;

        return url.substring(0, index);
    }

    @Override
    public String getTaskIdFromTaskUrl(String url) {
        if (url == null)
            return null;

        int index = url.lastIndexOf("/ticket/");
        if (index == -1)
            return null;

        return url.substring(index + "/ticket/".length() + 1);
    }

    @Override
    public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor) throws CoreException {
        return taskDataHandler.getTaskData(repository, taskId, monitor);
    }

    @Override
    public AbstractTaskDataHandler getTaskDataHandler() {
        return taskDataHandler;
    }

    @Override
    public String getTaskUrl(String repositoryUrl, String id) {
        if (repositoryUrl == null || id == null)
            return null;
        try {
            return URLFactory.create(repositoryUrl).getAPITicketUrl(id);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(repositoryUrl);
        }
    }

    @Override
    public boolean hasTaskChanged(TaskRepository repository, ITask task, TaskData taskData) {
        if (task == null || task.getModificationDate() == null)
            return false;

        final Date lastKnownDate = task.getModificationDate();
        final Date newDate = taskData.getAttributeMapper().getDateValue(
                taskData.getRoot().getAttribute(TaskAttribute.DATE_MODIFICATION));

        return (!lastKnownDate.equals(newDate));
    }

    @Override
    public IStatus performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector collector,
            ISynchronizationSession session, IProgressMonitor monitor) {
        try {
            monitor.beginTask("", 1);
            taskDataHandler.performQuery(repository, query, collector, monitor);
            return RepositoryStatus.OK_STATUS;
        } catch (CoreException e) {
            return e.getStatus();
        } finally {
            monitor.done();
        }
    }

    @Override
    public void updateRepositoryConfiguration(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
        RequestTrackerCorePlugin.getDefault().getConfigurationCache().refreshConfiguration(repository, monitor);
    }

    @Override
    public void updateTaskFromTaskData(TaskRepository repository, ITask task, TaskData taskData) {
        TaskMapper mapper = getTaskMapping(taskData);
        mapper.applyTo(task);
    }

    @Override
    public TaskMapper getTaskMapping(TaskData taskData) {
        return new TaskMapper(taskData);
    }

}
