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
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

import se.fnord.rt.core.internal.RTClient;
import se.fnord.rt.core.internal.RTException;
import se.fnord.rt.core.internal.RTHistory;
import se.fnord.rt.core.internal.RTHistoryAttributes;
import se.fnord.rt.core.internal.RTTicket;
import se.fnord.rt.core.internal.RTTicketAttributes;
import se.fnord.rt.core.internal.URLFactory;

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
        final TaskAttribute root = data.getRoot();

        createAttribute(root, TaskAttribute.SUMMARY, TaskAttribute.TYPE_SHORT_TEXT, "Summary", initializationData.getTaskKey());
        createAttribute(root, TaskAttribute.DESCRIPTION, TaskAttribute.TYPE_LONG_TEXT, "Description", initializationData.getDescription());
        TaskAttribute status = createAttribute(root, TaskAttribute.STATUS, TaskAttribute.TYPE_SINGLE_SELECT, "Status", "APA");
        
        status.putOption("APA", "APA");
        status.putOption("BANAN", "BANAN");
        
        return true;
    }

    @Override
    public RepositoryResponse postTaskData(TaskRepository repository, TaskData data, Set<TaskAttribute> arg2, IProgressMonitor arg3)
            throws CoreException {
        // TODO: Implement read-write functionality.
        return null;
    }

    private TaskAttribute createAttribute(TaskAttribute parent, String id, String type, String label, String value) {
        TaskAttribute attribute = parent.createAttribute(id);
        attribute.setValue(value);
        TaskAttributeMetaData meta = attribute.getMetaData(); 
        meta.setType(type);
        meta.setLabel(label);
        return attribute;
    }
    
    private TaskAttribute createComment(TaskAttribute parent, int n, int id, IRepositoryPerson author, Date date, String value) {        
        TaskAttribute attribute = parent.createAttribute(TaskAttribute.PREFIX_COMMENT + n);
        TaskCommentMapper comment = TaskCommentMapper.createFrom(attribute);
        comment.setCommentId(Integer.toString(id));
        comment.setNumber(n);
        comment.setText(value);
        comment.setCommentId("/" + n);
        comment.setUrl("#txn-" + n);
        comment.setAuthor(author);
        comment.setCreationDate(date);
        
        comment.applyTo(attribute);
        return attribute;
    }
    
    
    
    public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor) {
        monitor.beginTask("Fetching task #" + taskId, 1);
        final URLFactory urls;
        try {
            urls = URLFactory.create(repository.getRepositoryUrl());
        } catch (MalformedURLException e1) {
            throw new IllegalArgumentException(e1);
        }

        final RTClient client = RequestTrackerCorePlugin.getDefault().getClient(repository);
        try {
            final RTTicket task = client.getTask(taskId);
            monitor.worked(1);

            final TaskAttributeMapper mapper = getAttributeMapper(repository);
            final TaskData taskData = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), taskId);
            final TaskAttribute root = taskData.getRoot();
            for (Map.Entry<RTTicketAttributes, Object> field : task.fields.entrySet()) {
                field.getKey().createAttribute(mapper, root, field.getValue());
            }
            
            createAttribute(root, TaskAttribute.TASK_URL, null, null, urls.getBrowseTicketUrl(taskId));
            
            if (task.comments != null) {
                int i = 0;
                for (final RTHistory comment : task.comments) {
                    final String type = (String) comment.fields.get(RTHistoryAttributes.TYPE);
                    if ("Comment".equals(type) || "Correspond".equals(type))
                        createComment(root, i++, 
                                (Integer) comment.fields.get(RTHistoryAttributes.ID),
                                repository.createPerson((String) comment.fields.get(RTHistoryAttributes.CREATOR)),
                                (Date) comment.fields.get(RTHistoryAttributes.CREATED_TIME),
                                (String) comment.fields.get(RTHistoryAttributes.CONTENT));
                    else if ("Create".equals(type)) {
                        TaskAttribute attribute = root.createAttribute(TaskAttribute.DESCRIPTION);
                        attribute.setValue((String) comment.fields.get(RTHistoryAttributes.CONTENT));
                        TaskAttributeMetaData meta = attribute.getMetaData(); 
                        meta.setType(TaskAttribute.TYPE_LONG_RICH_TEXT);
                        meta.setLabel("Description");
                    }
                }
            }
            taskData.setPartial(task.partial);
            return taskData;

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
        monitor.beginTask("Query", 2);
        final URLFactory urls;
        try {
            urls = URLFactory.create(repository.getRepositoryUrl());
        } catch (MalformedURLException e1) {
            throw new IllegalArgumentException(e1);
        }

        final String queryString = query.getAttribute(QUERY_ID);
        final RTClient client = RequestTrackerCorePlugin.getDefault().getClient(repository);
        try {
            final List<RTTicket> tasks = client.getQuery(queryString);
            monitor.worked(1);
            final TaskAttributeMapper mapper = getAttributeMapper(repository);
            for (RTTicket task : tasks) {
                final TaskData taskData = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), Integer.toString(task.taskId));
                final TaskAttribute root = taskData.getRoot();
                
                createAttribute(root, TaskAttribute.TASK_URL, null, null, urls.getBrowseTicketUrl(Integer.toString(task.taskId)));
                
                for (Map.Entry<RTTicketAttributes, Object> field : task.fields.entrySet()) {
                    field.getKey().createAttribute(mapper, root, field.getValue());
                }
                
                if (task.comments != null) {
                    int i = 0;
                    for (final RTHistory comment : task.comments) {
                        final String type = (String) comment.fields.get(RTHistoryAttributes.TYPE);
                        if ("Comment".equals(type) || "Correspond".equals(type))
                            createComment(root, i++, 
                                    (Integer) comment.fields.get(RTHistoryAttributes.ID),
                                    repository.createPerson((String) comment.fields.get(RTHistoryAttributes.CREATOR)),
                                    (Date) comment.fields.get(RTHistoryAttributes.CREATED_TIME),
                                    (String) comment.fields.get(RTHistoryAttributes.CONTENT));
                        else if ("Create".equals(type)) {
                            TaskAttribute attribute = root.createAttribute(TaskAttribute.DESCRIPTION);
                            attribute.setValue((String) comment.fields.get(RTHistoryAttributes.CONTENT));
                            TaskAttributeMetaData meta = attribute.getMetaData(); 
                            meta.setType(TaskAttribute.TYPE_LONG_RICH_TEXT);
                            meta.setLabel("Description");
                        }
                    }
                }
                taskData.setPartial(task.partial);
                collector.accept(taskData);
            }

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
