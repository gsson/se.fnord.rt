package se.fnord.rt.core.internal;

import java.io.Serializable;
import java.util.List;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

public class CustomField implements Field, Serializable {
    private static final long serialVersionUID = 569206247221805055L;
    private static final String MYLYN_ID_PREFIX = "rt.fields.";
    private final String rtId;
    private final String label;
    private final String description;
    private final String mylynId;
    private final String translatorName;

    public CustomField(final String id, final String label, final String description, final String translatorName) {
        this.rtId = id;
        this.label = label;
        this.description = description;
        this.translatorName = translatorName;
        this.mylynId = MYLYN_ID_PREFIX + id;
    }

    @Override
    public String getRTId() {
        return rtId;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getKind() {
        return TaskAttribute.KIND_DEFAULT;
    }

    @Override
    public String getType() {
        return TaskAttribute.TYPE_SHORT_TEXT;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getMylynId() {
        return mylynId;
    }

    @Override
    public String getTranslatorName() {
        return translatorName;
    }

    @Override
    public List<Option> getOptions() {
        return null;
    }

}
