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
package se.fnord.rt.client.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

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
