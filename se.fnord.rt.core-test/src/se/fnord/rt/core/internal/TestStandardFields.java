package se.fnord.rt.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Collections;

import org.junit.Test;

public class TestStandardFields {

   @Test
   public void testLoad() throws Exception {
       StandardFields fields = new StandardFields("3.8.2", Collections.<QueueInfo>emptySet());
       fields.load();
       StandardField field = fields.getByMylynId("rt.fields.queue");
       StandardField field2 = fields.getByRTId("Queue");
       assertSame(field, field2);
       assertNotNull(field);
       assertEquals("Queue", field.getLabel());
       assertEquals("Ticket queue", field.getDescription());
       assertFalse(field.isReadOnly());
   }

   @Test
   public void testVersionComparator() {
       assertEquals(0, StandardFields.VERSION_COMPARATOR.compare("3.8.1", "3.8.1"));
       assertEquals(-1, StandardFields.VERSION_COMPARATOR.compare("3.8.1", "3.8.2"));
       assertEquals(1, StandardFields.VERSION_COMPARATOR.compare("3.8.2", "3.8.1"));
       assertEquals(-1, StandardFields.VERSION_COMPARATOR.compare("3.8.1", "3.8.1.1"));
       assertEquals(1, StandardFields.VERSION_COMPARATOR.compare("3.8.1.1", "3.8.1"));
   }

}
