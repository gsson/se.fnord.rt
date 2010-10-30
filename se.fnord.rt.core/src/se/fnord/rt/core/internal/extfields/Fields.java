package se.fnord.rt.core.internal.extfields;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="fields")
public class Fields {
    @XmlElements(@XmlElement(name="version", type=Version.class))
    public final List<Version> versions = null;
}