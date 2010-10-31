/*
 * Copyright (c) 2010 Henrik Gustafsson <henrik.gustafsson@fnord.se>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package se.fnord.rt.core.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlValue;

public class StandardField implements Field, Serializable {
    private static final long serialVersionUID = 735974018454589190L;

    public static final class OptionImpl implements Option, Serializable {
        private static final long serialVersionUID = 3737624977306657542L;

        @XmlAttribute
        private final String name;
        @XmlValue
        private final String label;

        public OptionImpl() {
            name = null;
            label = null;
        }
        public OptionImpl(final String name, final String title) {
            this.name = name;
            this.label = title;
        }

        /* (non-Javadoc)
         * @see se.fnord.rt.core.internal.Option#getTitle()
         */
        @Override
        public String getLabel() {
            return label;
        }

        /* (non-Javadoc)
         * @see se.fnord.rt.core.internal.Option#getName()
         */
        @Override
        public String getName() {
            return name;
        }
    }

    @XmlAttribute(name = "name")
    private final String rtId;
    @XmlAttribute
    private final String label;

    @XmlElement
    private final String description;

    @XmlElementWrapper(name="options")
    @XmlElements(@XmlElement(name="option", type=OptionImpl.class))
    private final List<Option> options;

    @XmlAttribute
    private final String kind;
    @XmlAttribute
    private final String type;
    @XmlAttribute
    private final boolean readOnly;
    @XmlAttribute
    private final String mylynId;
    @XmlAttribute(name = "mapper")
    private final String translatorName;

    public StandardField(final String mylynId, final String rtId, final String label, final String description, final String kind,
            final String type, final String translatorName, final boolean readOnly, final List<Option> options) {
        this.mylynId = mylynId;
        this.rtId = rtId;
        this.label = label;
        this.description = description;
        if (kind != null)
            this.kind = kind.substring(kind.lastIndexOf('.') + 1);
        else
            this.kind = null;
        this.type = type;
        this.translatorName = translatorName;
        this.readOnly = readOnly;
        this.options = Collections.unmodifiableList(new ArrayList<Option>(options));
    }

    public StandardField() {
        this.mylynId = null;
        this.rtId = null;
        this.label = null;
        this.description = null;
        this.kind = null;
        this.type = null;
        this.translatorName = null;
        this.readOnly = true;
        this.options = null;
    }

    @Override
    public String getRTId() {
        return rtId;
    }

    @Override
    public String getLabel() {
        return (label == null)?getRTId():label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getKind() {
        if (kind == null)
            return null;
        else
            return "task.common.kind." + kind;
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

    public List<Option> getOptions() {
        return options;
    }

}
