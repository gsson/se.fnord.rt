package se.fnord.rt.core.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StandardFields implements Fields, Serializable {
    private static final long serialVersionUID = 6127237818899938841L;
    private final String version;
    private List<StandardField> fields = Collections.emptyList();
    private transient Map<String, StandardField> fieldsByRTId = null;
    private transient Map<String, StandardField> fieldsByMylynId = null;

    public StandardFields(final String version) {
        this.version = version;
    }

    public void load() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
        /* TODO: Yea...do something proper here */

        final String resourcePath = "/se/fnord/rt/core/standardfields-v" + version + ".xml";

        final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document doc = db.parse(getClass().getResourceAsStream(resourcePath));
        doc.getDocumentElement().normalize();

        final NodeList nodes = doc.getElementsByTagName("field");

        List<StandardField> newFields = new ArrayList<StandardField>(nodes.getLength());
        fieldsByMylynId = null;
        fieldsByRTId = null;

        for (int s = 0; s < nodes.getLength(); s++) {

            Element fieldNode = (Element) nodes.item(s);

            final String name = fieldNode.getAttribute("name");
            final String mylynId = fieldNode.getAttribute("mylynId");
            final String type = fieldNode.getAttribute("type");
            final String kind = fieldNode.getAttribute("kind");
            final String label = (fieldNode.hasAttribute("label"))?fieldNode.getAttribute("label"):name;
            final boolean readOnly = Boolean.parseBoolean(fieldNode.getAttribute("label"));
            final String description;
            final NodeList descriptionNodes = fieldNode.getElementsByTagName("description");

            if (descriptionNodes == null || descriptionNodes.getLength() != 1)
                description = "";
            else
                description = descriptionNodes.item(0).getTextContent().trim();

            newFields.add(new StandardField(mylynId, name, label, description, kind, type, readOnly));
        }
        fields = Collections.unmodifiableList(newFields);
    }

    private void buildMaps() {
        this.fieldsByRTId = new HashMap<String, StandardField>();
        this.fieldsByMylynId = new HashMap<String, StandardField>();
        for (final StandardField field : fields) {
            fieldsByRTId.put(field.getRTId(), field);
            fieldsByMylynId.put(field.getMylynId(), field);
        }
    }

    @Override
    public List<StandardField> getFields() {
        return fields;
    }

    @Override
    public StandardField getByRTId(final String name) {
        if (fieldsByRTId == null)
            buildMaps();
        return fieldsByRTId.get(name);
    }

    @Override
    public StandardField getByMylynId(final String name) {
        if (fieldsByMylynId == null)
            buildMaps();
        return fieldsByMylynId.get(name);
    }
}
