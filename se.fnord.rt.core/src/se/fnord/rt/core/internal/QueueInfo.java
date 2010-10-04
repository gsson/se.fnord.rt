package se.fnord.rt.core.internal;

import java.io.Serializable;
import java.util.List;

public class QueueInfo implements Serializable {
    private static final long serialVersionUID = 6621528758030183289L;
    private final int id;
    private final String name;
    private final String description;
    private final CustomFields ticketCustomFields;

    public QueueInfo(final int id, final String name, final String description, final List<CustomField> ticketCustomFields) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ticketCustomFields = new CustomFields(ticketCustomFields);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public CustomFields getTicketCustomFields() {
        return ticketCustomFields;
    }
}
