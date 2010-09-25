package se.fnord.rt.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import se.fnord.rt.core.internal.ParseUtils;

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
        System.err.println(Arrays.toString("Apa\nb\nbanan".split("^b\\n")));
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

}