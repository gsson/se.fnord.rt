package se.fnord.rt.core.internal;

import java.util.List;


public interface Field {
    String getMylynId();

    String getRTId();
    String getLabel();
    String getDescription();

    String getKind();
    String getType();
    boolean isReadOnly();

    String getTranslatorName();

    List<Option> getOptions();
}
