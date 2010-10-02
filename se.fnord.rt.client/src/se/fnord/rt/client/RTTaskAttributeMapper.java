package se.fnord.rt.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;

public class RTTaskAttributeMapper extends TaskAttributeMapper {

    public RTTaskAttributeMapper(TaskRepository taskRepository) {
        super(taskRepository);
    }

}
