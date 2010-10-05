package se.fnord.rt.core.internal;

import java.util.List;

public interface Fields {
    Field getByRTId(final String rtId);
    Field getByMylynId(final String mylynId);
    List<? extends Field> getFields();
}
