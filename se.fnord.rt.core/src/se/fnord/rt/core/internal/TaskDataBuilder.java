package se.fnord.rt.core.internal;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import se.fnord.rt.client.RTHistory;
import se.fnord.rt.client.RTHistoryAttributes;
import se.fnord.rt.client.RTLinkType;
import se.fnord.rt.client.RTTicket;
import se.fnord.rt.client.URLFactory;
import se.fnord.rt.core.RequestTrackerCorePlugin;
import se.fnord.rt.core.internal.fields.DateField;
import se.fnord.rt.core.internal.fields.IdField;
import se.fnord.rt.core.internal.fields.IntegerField;
import se.fnord.rt.core.internal.fields.StringField;

public class TaskDataBuilder {
    private final URLFactory urls;
    private final TaskRepository repository;
    private final TaskAttributeMapper mapper;
    private final RepositoryConfiguration repositoryConfiguration;

    @SuppressWarnings("serial")
    public static final Map<String, FieldTranslator<?>> TRANSLATORS = new HashMap<String, FieldTranslator<?>>() {{
        put(FieldTranslator.TYPE_IDENTIFIER, new IdField("ticket"));
        put(FieldTranslator.TYPE_STRING, new StringField());
        put(FieldTranslator.TYPE_INTEGER, new IntegerField());
        put(FieldTranslator.TYPE_DATE, new DateField());
    }};

    public TaskDataBuilder(final TaskRepository repository, final RepositoryConfiguration repositoryConfiguration, final TaskAttributeMapper mapper) {
        this.repository = repository;
        this.repositoryConfiguration = repositoryConfiguration;
        this.mapper = mapper;
        try {
            urls = URLFactory.create(repository.getRepositoryUrl());
        } catch (MalformedURLException e1) {
            throw new IllegalArgumentException(e1);
        }
    }

    private static FieldTranslator<?> getTranslator(final Field field) {
        return TRANSLATORS.get(field.getTranslatorName());
    }

    private static <T> T translate(final Field field, final String value, Class<T> dstType) throws CoreException {
        try {
            if (value == null)
                return null;
            final FieldTranslator<T> t = (FieldTranslator<T>) getTranslator(field);
            if (t == null)
                throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, String.format("No translator for field %s/translator %s found", field.getLabel(), field.getTranslatorName()), null));
            return dstType.cast(t.objectRepresentation(value));
        }
        catch (ClassCastException e) {
            throw new CoreException(RepositoryStatus.createInternalError(RequestTrackerCorePlugin.PLUGIN_ID, "Invalid translation", e));
        }

    }

    private TaskAttribute createMappedAttribute(TaskAttribute parent, Field field, String values) throws CoreException {
        final String type = field.getType();
        TaskAttribute attribute = parent.createAttribute(field.getMylynId());
        TaskAttributeMetaData meta = attribute.getMetaData();
        meta.setType(type);
        meta.setLabel(field.getLabel());
        meta.setReadOnly(field.isReadOnly());
        if (field.getKind() != null)
            meta.setKind(field.getKind());
        if (values == null)
            attribute.clearValues();
        else {
            if (TaskAttribute.TYPE_INTEGER.equals(type))
                mapper.setIntegerValue(attribute, translate(field, values, Integer.class));
            else if (TaskAttribute.TYPE_DATETIME.equals(type))
                mapper.setDateValue(attribute, translate(field, values, Date.class));
            else if (TaskAttribute.TYPE_SHORT_TEXT.equals(type) || TaskAttribute.TYPE_LONG_TEXT.equals(type) || TaskAttribute.TYPE_LONG_RICH_TEXT.equals(type) || TaskAttribute.TYPE_SHORT_RICH_TEXT.equals(type))
                mapper.setValue(attribute, translate(field, values, String.class));
            else if (TaskAttribute.TYPE_PERSON.equals(type))
                mapper.setValue(attribute, translate(field, values, String.class));
            else if (TaskAttribute.TYPE_BOOLEAN.equals(type))
                mapper.setBooleanValue(attribute, translate(field, values, Boolean.class));
            else if (TaskAttribute.TYPE_SINGLE_SELECT.equals(type)) {
                /*
                for (String o : options)
                    attribute.putOption(o, o);
                */
                mapper.setValue(attribute, translate(field, values, String.class));
            }
        }
        return attribute;
    }


    public TaskData createTaskData(final RTTicket ticket) throws CoreException {
        final String taskId = Integer.toString(ticket.ticketId);
        final TaskData taskData = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), taskId);
        final TaskAttribute root = taskData.getRoot();

        StandardFields standardFields = repositoryConfiguration.getStandardFields();
        CustomFields customFields = repositoryConfiguration.getQueueInfo(ticket.queue).getTicketCustomFields();

        for (Field f : standardFields.getFields()) {
            createMappedAttribute(root, f, ticket.fields.get(f.getRTId()));
        }

        for (Field f : customFields.getFields()) {
            createMappedAttribute(root, f, ticket.fields.get(f.getRTId()));
        }

        if (ticket.links != null) {
            for (Map.Entry<RTLinkType, List<Integer>> link : ticket.links.entrySet()) {
                link.getKey().createAttribute(mapper, root, link.getValue()).getMetaData().setReadOnly(true);
            }
        }

        if (ticket.comments != null) {
            int i = 0;
            for (final RTHistory comment : ticket.comments) {
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
                    meta.setReadOnly(true);
                }
            }
            createAttribute(root, TaskAttribute.COMMENT_NEW, TaskAttribute.TYPE_LONG_RICH_TEXT, "New Comment", "");
        }

        taskData.setPartial(ticket.partial);

        return taskData;
    }


    private static TaskAttribute createAttribute(TaskAttribute parent, String id, String type, String label, String... values) {
        TaskAttribute attribute = parent.createAttribute(id);
        attribute.setValues(Arrays.asList(values));
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
