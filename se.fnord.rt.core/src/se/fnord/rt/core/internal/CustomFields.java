package se.fnord.rt.core.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomFields implements Fields, Serializable {
    private static final long serialVersionUID = 5752535170103086409L;
    private final List<CustomField> fields;
    private transient Map<String, CustomField> fieldsByRTId = null;
    private transient Map<String, CustomField> fieldsByMylynId = null;

    public CustomFields(final List<CustomField> fields) {
        this.fields = Collections.unmodifiableList(new ArrayList<CustomField>(fields));
    }

    @Override
    public CustomField getByRTId(final String rtId) {
        if (fieldsByRTId == null)
            buildMaps();
        return fieldsByRTId.get(rtId);
    }

    @Override
    public CustomField getByMylynId(final String mylynId) {
        if (fieldsByMylynId == null)
            buildMaps();
        return fieldsByMylynId.get(mylynId);
    }

    private void buildMaps() {
        fieldsByMylynId = new HashMap<String, CustomField>(fields.size());
        fieldsByRTId = new HashMap<String, CustomField>(fields.size());
        for (CustomField field : fields) {
            fieldsByMylynId.put(field.getMylynId(), field);
            fieldsByRTId.put(field.getRTId(), field);
        }
    }

    @Override
    public List<CustomField> getFields() {
        return fields;
    }

}
