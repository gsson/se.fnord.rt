package se.fnord.rt.core.internal;

import java.io.Serializable;

public class StandardField implements Field, Serializable {
    private static final long serialVersionUID = 735974018454589190L;
    private final String rtId;
    private final String label;
    private final String description;
    private final String kind;
    private final String type;
    private final boolean readOnly;
    private final String mylynId;
    private final String translatorName;

    public StandardField(final String mylynId, final String rtId, final String label, final String description, final String kind, final String type, final String translatorName, final boolean readOnly) {
        this.mylynId = mylynId;
        this.rtId = rtId;
        this.label = label;
        this.description = description;
        this.kind = kind;
        this.type = type;
        this.translatorName = translatorName;
        this.readOnly = readOnly;
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
        return kind;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public String getMylynId() {
        return mylynId;
    }

    @Override
    public String getTranslatorName() {
        return translatorName;
    }

}
