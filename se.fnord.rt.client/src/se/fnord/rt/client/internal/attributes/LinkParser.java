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
package se.fnord.rt.client.internal.attributes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class LinkParser implements RTAttributeParser<List<Integer>> {

    private static int parseLink(final String link) {
        return Integer.parseInt(link.substring(link.lastIndexOf('/') + 1));
    }

    @Override
    public List<Integer> parse(String value) {
        final String[] split = value.split(",");
        final ArrayList<Integer> links = new ArrayList<Integer>(split.length);
        for (String link : split)
            links.add(parseLink(link.trim()));
        return links;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String dump(Object value) {
        final List<Integer> links = (List<Integer>) value;
        return StringUtils.join(links, ", ");
    }

}
