package se.fnord.rt.core.internal;

import java.util.List;

public interface Fields {
    public static final String RT_FIELD_PREFIX = "rt.fields.";

    Field getByRTId(final String rtId);
    Field getByMylynId(final String mylynId);
    List<? extends Field> getFields();
}
