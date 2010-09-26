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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public final class ParseUtils {
    private ParseUtils() {}

    static String[] splitMultiPart(final String data) {
        return data.split("\\n--\\n");
    }

    static void parseMultiPart(String history, List<Map<String, String>> comments) {
        for (final String item : splitMultiPart(history)) {
            final Map<String, String> attributes = new HashMap<String, String>();
            ParseUtils.parseAttributes(item, attributes);
            ParseUtils.filterNotSet(attributes);
            comments.add(attributes);
        }
    }

    static void filterNotSet(final Map<String, String> attributes) {
        final Iterator<Map.Entry<String, String>> i = attributes.entrySet().iterator();
        while (i.hasNext()) {
            if ("Not set".equals(i.next().getValue()))
                i.remove();
        }
    }

    static void parseTicket(final String ticket, final Map<String, String> attributes) {
        ParseUtils.parseAttributes(ticket, attributes);
        filterNotSet(attributes);
    }

    static void putAttribute(final String attribute, final Map<String, String> attributes) {
        String[] split = attribute.split(":", 2);
        if (split.length == 1) {
            final String v = split[0].trim();
            attributes.put(v, v);
        } else if (split.length == 2) {
            final String n = split[0].trim();
            final String v = split[1].trim();
            attributes.put(n, v);
        } else
            throw new RuntimeException(String.format("Unexpected attribute format \"%s\"", attribute));
    }

    static void parseAttributes(final String attributeString, final Map<String, String> attributes) {
        final String[] lines = attributeString.split("\n");
        final StringBuilder headerBuilder = new StringBuilder();
        String previousLine = null;

        for (final String line : lines) {
            if (!line.isEmpty()) {
                if (line.startsWith(" ") || line.startsWith("\t")) {
                    // Continuation
                    if (previousLine != null) {
                        headerBuilder.append(previousLine);
                        previousLine = null;
                    }
                    headerBuilder.append("\n");
                    headerBuilder.append(line.trim());
                } else {
                    if (previousLine != null) {
                        putAttribute(previousLine, attributes);
                    } else if (headerBuilder.length() > 0) {
                        putAttribute(headerBuilder.toString(), attributes);
                        headerBuilder.setLength(0);
                    }
                    previousLine = line;
                }
            }
        }
        if (previousLine != null) {
            if (!previousLine.isEmpty())
                putAttribute(previousLine, attributes);
        } else if (headerBuilder.length() > 0) {
            putAttribute(headerBuilder.toString(), attributes);
            headerBuilder.setLength(0);
        }
    }

    public static String alignValue(final int alignment, final String value) {
        if (value == null)
            return "";

        final String trimmed = value.trim();
        final String[] lines = trimmed.split("\\n");
        if (lines.length == 1)
            return lines[0];
        final String separator = "\n" + StringUtils.repeat(" ", alignment);
        return StringUtils.join(lines, separator);
    }

    public static StringBuilder formatAttribute(final StringBuilder builder, String key, String value) {
        builder.append(key);
        builder.append(": ");
        builder.append(alignValue(key.length() + 2, value));
        return builder;
    }

    public static String generateAttributes(Map<String, String> changed) {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, String> attribute : changed.entrySet()) {
            formatAttribute(builder, attribute.getKey(), attribute.getValue());
            builder.append('\n');
        }
        return builder.toString();
    }
}
