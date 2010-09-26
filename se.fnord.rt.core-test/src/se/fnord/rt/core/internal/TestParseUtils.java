package se.fnord.rt.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.junit.Test;

public class TestParseUtils {

    @Test
    public void putAttribute() {
        final Map<String, String> result = new HashMap<String, String>();
        ParseUtils.putAttribute("A", result);
        assertEquals("A", result.get("A"));
        ParseUtils.putAttribute("A:B", result);
        assertEquals("B", result.get("A"));
        ParseUtils.putAttribute("A: B", result);
        assertEquals("B", result.get("A"));
        ParseUtils.putAttribute(" A : B ", result);
        assertEquals("B", result.get("A"));
        ParseUtils.putAttribute(" A : B \n C ", result);
        assertEquals("B \n C", result.get("A"));

    }

    @Test
    public void parseTicket() {
        final Map<String, String> result = new HashMap<String, String>();
        ParseUtils.parseAttributes("", result);
        assertTrue(result.isEmpty());
        ParseUtils.parseAttributes("apa: banan", result);
        assertEquals("banan", result.get("apa"));

        result.clear();
        ParseUtils.parseAttributes("apa: banan\n  apa", result);
        assertEquals("banan\napa", result.get("apa"));

        result.clear();
        ParseUtils.parseAttributes("apa: banan\nkorv:stroganoff", result);
        assertEquals("banan", result.get("apa"));
        assertEquals("stroganoff", result.get("korv"));
    }

    @Test
    public void testAlignValue() {
        assertEquals("", ParseUtils.alignValue(0, null));
        assertEquals("line1", ParseUtils.alignValue(0, "line1"));
        assertEquals("line1\n  line2", ParseUtils.alignValue(2, "line1\nline2"));
        assertEquals("line1\n   line2", ParseUtils.alignValue(3, "line1\nline2"));
        assertEquals("line1\n      line2\n      longerline3\n      line4", ParseUtils.alignValue(6, "line1\nline2\nlongerline3\nline4\n"));
    }

    @Test
    public void testFormatAttribute() {
        assertEquals(": line1", ParseUtils.formatAttribute(new StringBuilder(), "", "line1").toString());
        assertEquals(": line1\n  line2", ParseUtils.formatAttribute(new StringBuilder(), "", "line1\nline2").toString());
        assertEquals("A: line1\n   line2", ParseUtils.formatAttribute(new StringBuilder(), "A", "line1\nline2").toString());
        assertEquals("Apan: line1\n      line2\n      longerline3\n      line4", ParseUtils.formatAttribute(new StringBuilder(), "Apan", "line1\nline2\nlongerline3\nline4\n").toString());
        assertEquals("Apan: ", ParseUtils.formatAttribute(new StringBuilder(), "Apan", null).toString());
    }
}
