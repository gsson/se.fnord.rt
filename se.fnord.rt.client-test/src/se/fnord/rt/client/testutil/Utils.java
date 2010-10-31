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
package se.fnord.rt.client.testutil;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.CharBuffer;


public class Utils {
    private Utils() {}

    public static String getFile(final URL f) {
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(f.openStream());
            try {
                final StringBuilder fileData = new StringBuilder();
                final CharBuffer allocate = CharBuffer.allocate(1024);

                while(reader.read(allocate) != -1){
                    allocate.flip();
                    fileData.append(allocate);
                    allocate.clear();
                }

                return fileData.toString();
            }
            finally {
                reader.close();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
