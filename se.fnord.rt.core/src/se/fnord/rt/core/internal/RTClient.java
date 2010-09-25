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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;


public class RTClient {

    private final boolean anonymousLogin;
    private final String userName;
    private final String password;

    private Cookie session;
    private URLFactory urls;

    /* RT/3.8.2 401 Credentials required */
    private static final Pattern HEAD_PATTERN = Pattern.compile("RT/([0-9.]+) ([0-9]+) (.*)");

    public RTClient(TaskRepository repository) {

        try {
            urls = URLFactory.create(repository.getRepositoryUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        final AuthenticationCredentials credentials = repository.getCredentials(AuthenticationType.REPOSITORY);
        if (credentials == null) {
            anonymousLogin = true;
            userName = null;
            password = null;
        } else {
            anonymousLogin = false;
            userName = credentials.getUserName();
            password = credentials.getPassword();
        }
        session = null;
    }

    public RTTicket getTask(String id) throws HttpException, IOException, RTException {
        try {
            return getTaskInt(id);
        } catch (RTException e) {
            if (anonymousLogin || e.getCode() != 401)
                throw e;
            connect();
            return getTaskInt(id);
        }
    }

    private RTTicket getTaskInt(String id) throws HttpException, IOException, RTException {
        final String result = get(urls.getAPITicketUrl(id));
        final String history = get(urls.getAPITicketHistoryUrl(id));
        final RTTicket rtTicket = RTObjectFactory.createFullTicket(result, history);

        return rtTicket;
    }

    public RTUser getUser(String id) throws HttpException, IOException, RTException {
        try {
            return getUserInt(id);
        } catch (RTException e) {
            if (anonymousLogin || e.getCode() != 401)
                throw e;
            connect();
            return getUserInt(id);
        }
    }

    private RTUser getUserInt(String id) throws HttpException, IOException, RTException {
        final String result = get(urls.getAPIUserUrl(id));

        return RTObjectFactory.createUser(result);
    }

    public List<RTTicket> getQuery(String query) throws HttpException, IOException, RTException {
        try {
            return getQueryInt(query);
        } catch (RTException e) {
            if (anonymousLogin || e.getCode() != 401)
                throw e;
            connect();
            return getQueryInt(query);
        }
    }

    private List<RTTicket> getQueryInt(String query) throws HttpException, IOException, RTException {
        final String url = urls.getAPITicketSearch(query);
        
        final String result = get(url);

        return RTObjectFactory.createPartialTickets(result);
    }

    private void connect() throws HttpException, IOException {
        PostMethod getMethod = new PostMethod(urls.getAuthUrl());
        getMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2965);

        getMethod.setParameter("user", userName);
        getMethod.setParameter("pass", password);

        RTResponse response = execute(getMethod);
        if (response.getCode() != 200)
            throw new RuntimeException(response.message);
    }

    private String get(String url) throws HttpException, IOException, RTException {
        GetMethod getMethod = new GetMethod(url);
        getMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        final RTResponse response = execute(getMethod);
        if (response.getCode() != 200)
            throw new RTException(response.getCode(), response.getMessage());
        return execute(getMethod).getBody();
    }

    private RTResponse execute(HttpMethod method) throws HttpException, IOException {
        HttpState httpState = new HttpState();
        if (session != null)
            httpState.addCookie(session);

        HttpClient httpClient = new HttpClient();
        httpClient.setState(httpState);

        int code = httpClient.executeMethod(method);

        if (code != 200)
            throw new RuntimeException(String.format("Server returned code %d, expected 200", code));

        String body = method.getResponseBodyAsString();
        if (body == null)
            throw new RuntimeException("Server returned empty body");

        Cookie[] cookies = httpClient.getState().getCookies();

        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith("RT_SID"))
                session = cookie;
        }

        String[] split = body.split("\\n", 2);
        if (split.length > 0) {
            final String head = split[0];
            final Matcher matcher = HEAD_PATTERN.matcher(head);

            if (!matcher.matches())
                throw new RuntimeException(String.format("Invalid RT response header (\"%s\").", head));

            final String version = matcher.group(1);
            final int rtCode = Integer.parseInt(matcher.group(2));
            final String message = matcher.group(3);

            return new RTResponse(rtCode, message, version, split[1]);
        }
        throw new RuntimeException("Invalid body");
    }

}
