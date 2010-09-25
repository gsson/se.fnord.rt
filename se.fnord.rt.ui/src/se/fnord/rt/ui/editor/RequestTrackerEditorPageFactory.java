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

package se.fnord.rt.ui.editor;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;

import se.fnord.rt.core.RequestTrackerRepositoryConnector;

public class RequestTrackerEditorPageFactory extends AbstractTaskEditorPageFactory {

    public RequestTrackerEditorPageFactory() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean canCreatePageFor(TaskEditorInput input) {
        ITask task = input.getTask();
        return task.getConnectorKind().equals(RequestTrackerRepositoryConnector.REPOSITORY_CONNECTOR_KIND)
                || TasksUiUtil.isOutgoingNewTask(task, RequestTrackerRepositoryConnector.REPOSITORY_CONNECTOR_KIND);
    }

    @Override
    public Image getPageImage() {
        return null;
    }

    @Override
    public String getPageText() {
        return RequestTrackerRepositoryConnector.REPOSITORY_TYPE_LABEL;
    }

    @Override
    public IFormPage createPage(TaskEditor parentEditor) {
        return new RTTaskEditorPage(parentEditor);
    }

    @Override
    public String[] getConflictingIds(TaskEditorInput input) {
        if (!input.getTask().getConnectorKind().equals(RequestTrackerRepositoryConnector.REPOSITORY_CONNECTOR_KIND))
            return new String[] { ITasksUiConstants.ID_PAGE_PLANNING };
        return null;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY_TASK;
    }
}
