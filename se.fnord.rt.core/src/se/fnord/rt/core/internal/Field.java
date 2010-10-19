package se.fnord.rt.core.internal;

public interface Field {
    String getMylynId();

    String getRTId();
    String getLabel();
    String getDescription();

    String getKind();
    String getType();
    boolean isReadOnly();

    String getTranslatorName();
}
