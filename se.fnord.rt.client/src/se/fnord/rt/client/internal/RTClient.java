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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import se.fnord.rt.client.RTAuthenticationException;
import se.fnord.rt.client.RTException;
import se.fnord.rt.client.URLFactory;

public class RTClient implements RTRequests, RTExecutor {

    private final HttpConnectionManager connectionManager;
    private final boolean anonymousLogin;
    private final String userName;
    private final String password;
    private final URLFactory urls;
    private final ExecutorService executor;

    private Cookie session;
    private String authFailed;

    /* RT/3.8.2 401 Credentials required */
    private static final Pattern HEAD_PATTERN = Pattern.compile("RT/([0-9.]+) ([0-9]+) (.*)");
    private static final Pattern LINE_SPLITTER = Pattern.compile("\\n");

    public RTClient(final HttpConnectionManager connectionManager, ExecutorService executor, final String repositoryUrl, final String userName, final String password) {

        this.connectionManager = connectionManager;
        this.executor = executor;
        try {
            urls = URLFactory.create(repositoryUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        this.userName = userName;
        this.password = password;
        this.anonymousLogin = (userName == null || password == null);
        this.session = null;

        this.authFailed = null;
    }

    @Override
    public String[] awaitMany(Future<String>... futures) throws HttpException, IOException, RTException, InterruptedException {
        final String[] result = new String[futures.length];
        for (int i = 0; i < futures.length; i++)
            result[i] = await(futures[i]);
        return result;
    };

    @Override
    public String await(Future<String> future) throws HttpException, IOException, RTException, InterruptedException {
        try {
            return future.get();
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException)
                throw (IOException) cause;
            throw new RuntimeException(e);
        }
    };

    @Override
    public <T> T execute(RTOperation<T> operation) throws HttpException, IOException, RTException, InterruptedException {
            return operation.execute(urls, this);
    }

    private synchronized void checkAuthFailed() throws RTAuthenticationException {
        if (authFailed != null)
            throw new RTAuthenticationException(authFailed);
    }

    private synchronized void authenticate(final String message) throws HttpException, IOException, RTException {
        if (anonymousLogin) {
            authFailed = message;
            checkAuthFailed();
        }

        PostMethod method = new PostMethod(urls.getAuthUrl());
        try {
            method.getParams().setContentCharset("UTF-8");
            method.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
            method.addParameters(new NameValuePair[] {
                new NameValuePair("user", userName),
                new NameValuePair("pass", password)
            });

            execute(method);
        }
        catch (RTAuthenticationException e) {
            authFailed = e.getMessage();
            throw e;
        }
        finally {
            method.releaseConnection();
        }
    }

    private abstract class HttpCallable implements Callable<String> {
        protected final NameValuePair[] params;
        protected final String url;

        public HttpCallable(final String url, final NameValuePair... params) {
            this.url = url;
            this.params = params;
        }

        abstract protected HttpMethod createMethod(final String url, final NameValuePair... params);

        public final String call() throws Exception {
            try {
                return execute(createMethod(url, params)).getBody();
            } catch (RTAuthenticationException e) {
                authenticate(e.getMessage());
                return execute(createMethod(url, params)).getBody();
            }

        }
    }

    private final class PostCallable extends HttpCallable {
        public PostCallable(final String url, final NameValuePair... params) {
            super(url, params);
        }

        @Override
        protected HttpMethod createMethod(String url, NameValuePair... params) {
            PostMethod method = new PostMethod(url);
            method.getParams().setContentCharset("UTF-8");
            method.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
            method.addParameters(params);
            return method;
        }
    }

    private final class GetCallable extends HttpCallable {
        public GetCallable(final String url, final NameValuePair... params) {
            super(url, params);
        }

        @Override
        protected HttpMethod createMethod(String url, NameValuePair... params) {
            GetMethod method = new GetMethod(url);
            method.getParams().setContentCharset("UTF-8");
            method.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
            if (params != null && params.length > 0)
                method.setQueryString(params);
            return method;
        }
    }

    private Future<String> submit(HttpCallable c) {
        while (true) {
            try {
                return executor.submit(c);
            }
            catch (RejectedExecutionException e) {
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Future<String> post(final String url, final NameValuePair... params) {
        return submit(new PostCallable(url, params));
    }

    @Override
    public Future<String> get(String url, final NameValuePair... params) throws HttpException, IOException, RTException {
        return submit(new GetCallable(url, params));
    }

    private RTResponse execute(HttpMethod method) throws HttpException, IOException, RTException {

        final HttpClient httpClient = new HttpClient(connectionManager);

        final HttpState httpState = new HttpState();
        if (session != null)
            httpState.addCookie(session);
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

        String[] split = LINE_SPLITTER.split(body, 2);
        if (split.length > 0) {
            final String head = split[0];
            final Matcher matcher = HEAD_PATTERN.matcher(head);

            if (!matcher.matches())
                throw new RuntimeException(String.format("Invalid RT response header (\"%s\").", head));

            final String version = matcher.group(1);
            final int rtCode = Integer.parseInt(matcher.group(2));
            final String message = matcher.group(3);

            if (rtCode != 200) {
                if (rtCode == 401)
                    throw new RTAuthenticationException(message);
                throw new RTException(rtCode, message);
            }

            return new RTResponse(rtCode, message, version, split[1]);
        }
        throw new RuntimeException("Invalid body");
    }
}
