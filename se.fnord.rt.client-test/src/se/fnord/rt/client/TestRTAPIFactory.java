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
package se.fnord.rt.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestRTAPIFactory {
    @Test
    public void createRepositoryKey() {
        assertEquals("http://B@A/", RTAPIFactory.createRepositoryKey("http://A", "B"));
        assertEquals("http://B@apa.banan.se:8080/", RTAPIFactory.createRepositoryKey("http://apa.banan.se:8080/", "B"));
        assertEquals("http://A%20B@apa.banan.se/", RTAPIFactory.createRepositoryKey("http://apa.banan.se/", "A B"));
    }
}
