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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public final class URLFactory {
    private static final Path API_PATH = new Path("/REST/1.0/");
    private static final Path BROWSE_PATH = new Path("/");
    private final Path basePath; 
    private final URL repositoryURL;
    
    public static URLFactory create(final String repositoryURL) throws MalformedURLException {
        return new URLFactory(new URL(repositoryURL));
    }

    private URLFactory(final URL repositoryURL) {
        this.repositoryURL = repositoryURL;
        if (this.repositoryURL.getFile().isEmpty())
            this.basePath = new Path("/");
        else
            this.basePath = new Path(this.repositoryURL.getFile());
    }

    private URL appendPath(final IPath path, final Map<String, String> query) {
        try {
            if (!query.isEmpty())
                return new URL(repositoryURL.getProtocol(), repositoryURL.getHost(), repositoryURL.getPort(), basePath.append(path).toString() + makeQuery(query));
            else
                return appendPath(path);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Map<String, String> makeMap(final String...kvpairs) {
        if (kvpairs == null || kvpairs.length == 0)
            return Collections.emptyMap();
        else if ((kvpairs.length & 1) != 0)
            throw new IllegalArgumentException();

        final HashMap<String,String> map = new HashMap<String, String>();
        for (int i = 0; i < kvpairs.length; i += 2)
            map.put(kvpairs[i], kvpairs[i + 1]);

        return map;
    }
    
    private URL appendPath(final IPath path, final String...kvpairs) {
        return appendPath(path, makeMap(kvpairs));
    }

    private URL appendPath(final IPath path) {
        try {
            return new URL(repositoryURL.getProtocol(), repositoryURL.getHost(), repositoryURL.getPort(), basePath.append(path).toString());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    private String encode(final String in) {
        try {
            return URLEncoder.encode(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String makeQuery(final Map<String, String> query) {
        final StringBuilder sb = new StringBuilder("?");
        final Iterator<Map.Entry<String, String>> i = query.entrySet().iterator();
        if (i.hasNext()) {
            Map.Entry<String, String> e = i.next();
            sb.append(encode(e.getKey()));
            sb.append('=');
            sb.append(encode(e.getValue()));
            while (i.hasNext()) {
                e = i.next();
                sb.append('&');
                sb.append(encode(e.getKey()));
                sb.append('=');
                sb.append(encode(e.getValue()));
            }
        }
        return sb.toString();
    }
    
    public String getAuthUrl() {
        return appendPath(API_PATH).toString();
    }
    
    public String getAPITicketHistoryUrl(final String ticketId) {
        return appendPath(API_PATH.append("ticket").append(ticketId).append("history"), "format", "l").toString();
    }
    
    public String getAPITicketUrl(final String ticketId) {
        return appendPath(API_PATH.append("ticket").append(ticketId)).toString();
    }
    
    public String getBrowseTicketUrl(final String ticketId) {
        return appendPath(BROWSE_PATH.append("Ticket").append("Display.html"), "id", ticketId).toString();
    }
    
    public String getAPITicketSearch(final String filter) {
        return appendPath(API_PATH.append("search").append("ticket"), "format", "l", "query", filter).toString();
    }
    
    public String getBrowseTicketSearch(final String filter) {
        return appendPath(BROWSE_PATH.append("Search").append("Results.html"), "Query", filter).toString();
    }

    public String getAPIUserUrl(String id) {
        return appendPath(API_PATH.append("user").append(id), "format", "l").toString();
    }

    public String getBrowseUserUrl(String id) {
        return appendPath(BROWSE_PATH.append("Prefs.html"), "id", id).toString();
    }
}
