package se.fnord.rt.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public interface RepositoryConfigurationFetcher {
    public RepositoryConfiguration fetch(TaskRepository repository, IProgressMonitor monitor) throws CoreException;
}
