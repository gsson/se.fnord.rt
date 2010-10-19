package se.fnord.rt.core.internal.extfields;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import se.fnord.rt.core.internal.StandardField;

public class Version {
    @XmlAttribute
    public String minVersion;
    @XmlAttribute
    public String maxVersion;

    @XmlElements(@XmlElement(name="field", type=StandardField.class))
    public List<StandardField> fields;
}
