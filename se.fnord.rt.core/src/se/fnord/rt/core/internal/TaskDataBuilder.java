package se.fnord.rt.core.internal;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

public class TaskDataBuilder {
    private final URLFactory urls;
    private final TaskRepository repository;
    private final TaskAttributeMapper mapper;


    public TaskDataBuilder(final TaskRepository repository, final TaskAttributeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
        try {
            urls = URLFactory.create(repository.getRepositoryUrl());
        } catch (MalformedURLException e1) {
            throw new IllegalArgumentException(e1);
        }
    }


    public TaskData createTaskData(final RTTicket task) {
        final String taskId = Integer.toString(task.taskId);
        final TaskData taskData = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), taskId);
        final TaskAttribute root = taskData.getRoot();

        for (Map.Entry<RTTicketAttributes, Object> field : task.mappedFields.entrySet()) {
            field.getKey().createAttribute(mapper, root, field.getValue());
        }

        for (Map.Entry<String, String> field : task.unmappedFields.entrySet()) {
            RTTicketAttributes.createDefaultAttribute(mapper, root, field.getKey(), field.getValue());
        }

        createAttribute(root, TaskAttribute.TASK_URL, null, null, urls.getBrowseTicketUrl(taskId));

        for (Map.Entry<RTLinkType, List<Integer>> link : task.links.entrySet()) {
            link.getKey().createAttribute(mapper, root, link.getValue());
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
            createAttribute(root, TaskAttribute.COMMENT_NEW, TaskAttribute.TYPE_LONG_RICH_TEXT, "New Comment", "");
        }
        taskData.setPartial(task.partial);
        return taskData;

    }


    private static TaskAttribute createAttribute(TaskAttribute parent, String id, String type, String label, String value) {
        TaskAttribute attribute = parent.createAttribute(id);
        attribute.setValue(value);
        TaskAttributeMetaData meta = attribute.getMetaData();
        meta.setType(type);
        meta.setLabel(label);
        return attribute;
    }

    private static TaskAttribute createComment(TaskAttribute parent, int n, int id, IRepositoryPerson author, Date date, String value) {
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
}
