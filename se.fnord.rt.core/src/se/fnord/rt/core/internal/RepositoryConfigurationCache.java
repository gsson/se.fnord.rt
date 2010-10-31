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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import se.fnord.rt.client.RTAPIFactory;

public class RepositoryConfigurationCache {

    private final Map<String, RepositoryConfiguration> cache;
    private final RepositoryConfigurationFetcher fetcher;
    private final File configurationFile;
    private boolean configurationImported;

    public RepositoryConfigurationCache(final RepositoryConfigurationFetcher fetcher, final File configurationFile) {
        this.fetcher = fetcher;
        this.configurationFile = configurationFile;
        this.cache = new HashMap<String, RepositoryConfiguration>();
        this.configurationImported = false;
    }

    private static String makeKey(final TaskRepository repository) {
        return RTAPIFactory.createRepositoryKey(repository.getRepositoryUrl(), repository.getUserName());
    }

    public synchronized RepositoryConfiguration getConfiguration(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
        if (!configurationImported)
            importCache(configurationFile, monitor);
        final RepositoryConfiguration configuration = cache.get(makeKey(repository));
        if (configuration == null)
            return refreshConfiguration(repository, monitor);

        return configuration;
    }

    public synchronized RepositoryConfiguration refreshConfiguration(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
        final RepositoryConfiguration configuration = fetcher.fetch(repository, monitor);

        cache.put(configuration.getRepositoryKey(), configuration);

        return configuration;
    }

    public synchronized RepositoryConfiguration removeConfiguration(TaskRepository repository) {
        return cache.remove(makeKey(repository));
    }

    void importCache(File f, IProgressMonitor monitor) {
        ObjectInputStream ois = null;
        try {
            if (!f.exists()) {
                configurationImported = true;
                return;
            }

            ois = new ObjectInputStream(new FileInputStream(f));
            final int count = ois.readInt();
            cache.clear();
            for (int i = 0; i < count; i++) {
                final RepositoryConfiguration configuration = (RepositoryConfiguration) ois.readObject();
                cache.put(configuration.getRepositoryKey(), configuration);
            }
            configurationImported = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void exportCache(File f) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeInt(cache.size());
            for (RepositoryConfiguration configuration : cache.values())
                oos.writeObject(configuration);
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                if (oos != null)
                    oos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized public void close() {
        exportCache(configurationFile);
        cache.clear();
        configurationImported = false;
    }
}
