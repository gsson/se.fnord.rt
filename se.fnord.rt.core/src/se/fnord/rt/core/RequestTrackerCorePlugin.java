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

import org.eclipse.core.runtime.Plugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.osgi.framework.BundleContext;

import se.fnord.rt.core.internal.RTClient;
import se.fnord.rt.core.internal.RTClientFactory;

public class RequestTrackerCorePlugin extends Plugin {

    public static final String PLUGIN_ID = "se.fnord.rt.core";

    private static RequestTrackerCorePlugin plugin;

    private RequestTrackerRepositoryConnector connector;

    private TaskRepositoryLocationFactory taskRepositoryLocationFactory;

    private RTClientFactory clientFactory = new RTClientFactory();

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
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

    public RTClient getClient(TaskRepository repo) {
        return clientFactory.getClient(repo);
    }
    
    public void setTaskRepositoryLocationFactory(TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
        this.taskRepositoryLocationFactory = taskRepositoryLocationFactory;
    }

    public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
        return taskRepositoryLocationFactory;
    }

}
