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
package se.fnord.rt.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.osgi.framework.BundleContext;

import se.fnord.rt.client.RTAPI;
import se.fnord.rt.client.RTAPIFactory;
import se.fnord.rt.core.internal.RepositoryConfigurationCache;
import se.fnord.rt.core.internal.RepositoryConfigurationFetcherImpl;

public class RequestTrackerCorePlugin extends Plugin {

    public static final String PLUGIN_ID = "se.fnord.rt.core";

    private static RequestTrackerCorePlugin plugin;

    private RequestTrackerRepositoryConnector connector = null;
    private RepositoryConfigurationCache configurationCache = null;

    private TaskRepositoryLocationFactory taskRepositoryLocationFactory;

    private RTAPIFactory clientFactory = new RTAPIFactory();

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        configurationCache.close();
        configurationCache = null;
        super.stop(context);
    }

    public static RequestTrackerCorePlugin getDefault() {
        return plugin;
    }

    public synchronized RequestTrackerRepositoryConnector getConnector() {
        if (connector == null)
            connector = new RequestTrackerRepositoryConnector();

        return connector;
    }

    public synchronized RepositoryConfigurationCache getConfigurationCache() {
        if (configurationCache == null)
            configurationCache = new RepositoryConfigurationCache(new RepositoryConfigurationFetcherImpl(), getConfigurationCachePath().toFile());

        return configurationCache;
    }

    public RTAPI getClient(TaskRepository repo) {
        AuthenticationCredentials credentials = repo.getCredentials(AuthenticationType.REPOSITORY);
        return clientFactory.getClient(repo.getRepositoryUrl(), credentials.getUserName(), credentials.getPassword());
    }

    public void setTaskRepositoryLocationFactory(TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
        this.taskRepositoryLocationFactory = taskRepositoryLocationFactory;
    }

    public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
        return taskRepositoryLocationFactory;
    }

    IPath getConfigurationCachePath() {
        final IPath stateLocation = Platform.getStateLocation(getBundle());
        final IPath configurationFile = stateLocation.append("repositoryConfigurations");
        return configurationFile;
    }

}
